package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
import grill24.hoopyfroodtut.blockentity.MovingBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Begging Item Scrabbler is an autonomous item-collector block.
 * <p>
 * When placed (and fuelled with metallic nuggets) it:
 * <ol>
 *   <li>Searches for {@link net.minecraft.world.entity.item.ItemEntity ItemEntities}
 *       within its {@link BeggingItemScrabblerBlockEntity#SEARCH_RADIUS}-block radius.</li>
 *   <li>Advances toward the nearest item one block at a time, consuming one nugget per move.</li>
 *   <li>Picks up the item when it reaches the same block, dropping it back at its feet
 *       so players (or hoppers) can collect it.</li>
 * </ol>
 * Nuggets are added by right-clicking with an iron or gold nugget in hand.
 * They can also be pre-loaded by crafting the item with nuggets.
 */
public class BeggingItemScrabbler extends BaseEntityBlock {

    public static final MapCodec<BeggingItemScrabbler> CODEC = simpleCodec(BeggingItemScrabbler::new);

    /** Tracks the direction the scrabbler last moved (or is currently moving). */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BeggingItemScrabbler(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    // -------------------------------------------------------------------------
    // Player interaction — right-click with nugget to add fuel;
    //                      shift+right-click to extract held items
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        // Shift+right-click always falls through to useWithoutItem for item extraction
        if (player.isShiftKeyDown()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        boolean isNugget = stack.is(Items.IRON_NUGGET) || stack.is(Items.GOLD_NUGGET);
        boolean isChest = stack.is(Items.CHEST);
        if (!isNugget && !isChest) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof BeggingItemScrabblerBlockEntity be) {
            if (isNugget) {
                int toAdd = stack.getCount();

                if (stack.is(Items.IRON_NUGGET)) toAdd *= 2;
                else if (stack.is(Items.GOLD_NUGGET)) toAdd *= 8;

                be.addNuggets(toAdd);
                if (!player.isCreative()) stack.shrink(toAdd);
            } else if (isChest) {
                be.upgradeOneSlot();
                if (!player.isCreative()) stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            if (level.getBlockEntity(pos) instanceof BeggingItemScrabblerBlockEntity be && be.hasItems()) {
                be.extractItemsToPlayer(player, level, pos, state);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Drops — encode nuggets and home pos into the item on manual break
    // -------------------------------------------------------------------------

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof BeggingItemScrabblerBlockEntity scrabbler) {
            ItemStack drop = new ItemStack(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get());
            if (scrabbler.getNuggets() > 0)
                drop.set(HoopyFroodDataComponents.SCRABBLER_NUGGETS.get(), scrabbler.getNuggets());
            if (scrabbler.getHomePos() != null)
                drop.set(HoopyFroodDataComponents.SCRABBLER_HOME.get(), scrabbler.getHomePos());
            if (scrabbler.getInventory().size() > BeggingItemScrabblerBlockEntity.DEFAULT_INVENTORY_SIZE)
                drop.set(HoopyFroodDataComponents.SCRABBLER_SLOTS.get(), scrabbler.getInventory().size());
            // Carried inventory items drop separately as loose stacks
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            drops.add(drop);
            for (ItemStack stack : scrabbler.getInventory()) {
                if (!stack.isEmpty()) drops.add(stack.copy());
            }
            return drops;
        }
        return List.of(new ItemStack(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get()));
    }

    // -------------------------------------------------------------------------
    // Redstone — comparator output proportional to inventory fullness
    // -------------------------------------------------------------------------

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof BeggingItemScrabblerBlockEntity be) {
            return be.getRedstoneSignal();
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BeggingItemScrabblerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.BEGGING_ITEM_SCRABBLER.get(),
                BeggingItemScrabblerBlockEntity::tick);
    }

    // Bounding box enclosing the head elements (headBase + pupil) in neutral orientation
    private static final VoxelShape HEAD_SHAPE = Block.box(6, 11, 6, 10, 15, 10);
    private static final AABB HEAD_AABB =
            new AABB(6 / 16.0, 11 / 16.0, 6 / 16.0, 10 / 16.0, 15 / 16.0, 10 / 16.0);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof Level worldLevel
                && worldLevel.getBlockEntity(pos) instanceof MovingBlockEntity be) {
            float offset = be.getForwardOffset(worldLevel.getGameTime(), 0f);
            if (offset != 0f) {
                Direction dir = be.getMoveDirection();
                double dx = dir.getStepX() * offset;
                double dy = dir.getStepY() * offset;
                double dz = dir.getStepZ() * offset;
                return Shapes.create(HEAD_AABB.move(dx, dy, dz));
            }
        }
        return HEAD_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
