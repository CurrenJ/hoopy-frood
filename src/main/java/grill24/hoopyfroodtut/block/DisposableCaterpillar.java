package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.blockentity.MovingBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Disposable Caterpillar is a redstone-activated mining block.
 * <p>
 * When it receives a rising redstone edge it:
 * <ol>
 *   <li>Mines the block in its {@link #FACING} direction (with normal drops).</li>
 *   <li>Places a new Disposable Caterpillar in that space with one fewer charge,
 *       if the current block has more than one charge remaining.</li>
 *   <li>Destroys itself, dropping nothing.</li>
 * </ol>
 * Multiple caterpillar items can be combined in a crafting grid to produce a single item
 * with the combined charge count (see {@code CaterpillarCombineRecipe}).
 * <p>
 * When broken by a player (not by the self-destruct) the item drops with its current
 * charge count encoded as the {@code hoopyfroodtut:caterpillar_charges} data component.
 */
public class DisposableCaterpillar extends BaseEntityBlock {

    public static final MapCodec<DisposableCaterpillar> CODEC = simpleCodec(DisposableCaterpillar::new);

    /** Direction the caterpillar faces; it will mine the block in this direction. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    /**
     * True while the caterpillar has seen (and is processing) the current redstone
     * pulse. Acts as a persistent "already triggered" flag so a single high-signal
     * causes exactly one mining operation, even across server restarts.
     */
    public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

    public DisposableCaterpillar(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    // -------------------------------------------------------------------------
    // Placement — faces the direction the player is looking
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection())
                .setValue(TRIGGERED, false);
    }

    // -------------------------------------------------------------------------
    // Drops — include charges in the returned item when broken by a player
    // -------------------------------------------------------------------------

    /**
     * Returns a single caterpillar item with its charge count when broken by a
     * player.  The self-destruct path uses {@code level.destroyBlock(pos, false)}
     * which bypasses drops entirely, so this method is only reached on manual breaks.
     */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof DisposableCaterpillarBlockEntity caterpillar) {
            ItemStack drop = new ItemStack(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get());
            drop.set(HoopyFroodDataComponents.CATERPILLAR_CHARGES.get(), caterpillar.getCharges());
            if (caterpillar.getTorches() > 0) {
                drop.set(HoopyFroodDataComponents.CATERPILLAR_TORCHES.get(), caterpillar.getTorches());
            }
            return List.of(drop);
        }
        return List.of(new ItemStack(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get()));
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DisposableCaterpillarBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.DISPOSABLE_CATERPILLAR.get(),
                DisposableCaterpillarBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level instanceof Level worldLevel
                && worldLevel.getBlockEntity(pos) instanceof MovingBlockEntity be) {
            return MovingBlockEntity.animatedShape(worldLevel.getGameTime(), be);
        }
        return Shapes.block();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
