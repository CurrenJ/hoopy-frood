package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.InfiniteImprobabilityDriveBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Infinite Improbability Drive accepts a single input item and, over some
 * configurable time, converts it into a completely random item from the entire
 * game registry. Conversion speed scales with the number of distinct block types
 * surrounding the machine — the more variety, the faster it works.
 *
 * <p>Player interaction:
 * <ul>
 *   <li>Right-click with item in hand when machine is empty → insert item.</li>
 *   <li>Right-click (any hand state) when machine is occupied → eject the item.</li>
 * </ul>
 */
public class InfiniteImprobabilityDrive extends BaseEntityBlock {

    public static final MapCodec<InfiniteImprobabilityDrive> CODEC = simpleCodec(InfiniteImprobabilityDrive::new);

    public InfiniteImprobabilityDrive(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // Player interaction
    // -------------------------------------------------------------------------

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        // If the machine already holds an item, fall through to eject via useWithoutItem.
        if (level.getBlockEntity(pos) instanceof InfiniteImprobabilityDriveBlockEntity be
                && !be.getHeldItem().isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof InfiniteImprobabilityDriveBlockEntity be) {
            ItemStack toInsert = stack.copyWithCount(1);
            if (!player.isCreative()) stack.shrink(1);
            be.setHeldItem(toInsert);
            be.resetProgress();
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof InfiniteImprobabilityDriveBlockEntity be) {
            ItemStack held = be.getHeldItem();
            if (!held.isEmpty()) {
                be.setHeldItem(ItemStack.EMPTY);
                be.resetProgress();
                if (!player.getInventory().add(held)) {
                    player.drop(held, false);
                }
                return InteractionResult.SUCCESS_SERVER;
            } else if(player.isShiftKeyDown()) {
                be.getInfoComponent().forEach(player::sendSystemMessage);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Drops — eject the held item alongside the block itself
    // -------------------------------------------------------------------------

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(HoopyFroodItems.INFINITE_IMPROBABILITY_DRIVE_ITEM.get()));
        if (be instanceof InfiniteImprobabilityDriveBlockEntity drive) {
            ItemStack held = drive.getHeldItem();
            if (!held.isEmpty()) drops.add(held.copy());
        }
        return drops;
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfiniteImprobabilityDriveBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.INFINITE_IMPROBABILITY_DRIVE.get(),
                InfiniteImprobabilityDriveBlockEntity::tick);
    }

    // -------------------------------------------------------------------------
    // Rendering — fully custom; blockstate model is unused
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.box(3/16.0, 0/16.0, 3/16.0, 13/16.0, 5/16.0, 13/16.0);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}
