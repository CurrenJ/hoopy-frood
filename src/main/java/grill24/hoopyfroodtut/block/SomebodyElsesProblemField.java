package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.SomebodyElsesProblemFieldBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * The Somebody Else's Problem Field.
 * <p>
 * When placed, it creates an invisible null zone (radius {@link grill24.hoopyfroodtut.core.SepFieldManager#FIELD_RADIUS}
 * blocks) in which mob AI simply cannot function. When powered by redstone, the field's rings
 * animate upward and the floaters begin orbiting above them.
 */
public class SomebodyElsesProblemField extends BaseEntityBlock {

    public static final MapCodec<SomebodyElsesProblemField> CODEC =
            simpleCodec(SomebodyElsesProblemField::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** Flat slab matching the outer ring footprint when inactive. */
    private static final VoxelShape SHAPE_OFF = Block.box(1, 0, 1, 15, 1, 15);

    /** Taller box covering rings + floaters when active (inner ring peaks ~7/16, floaters ~12/16). */
    private static final VoxelShape SHAPE_ON  = Block.box(1, 4, 1, 15, 12, 15);

    public SomebodyElsesProblemField(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED,
                context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof SomebodyElsesProblemFieldBlockEntity be) {
                be.setActive(powered);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Block entity — no tick needed
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SomebodyElsesProblemFieldBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    // -------------------------------------------------------------------------
    // Shape / hitbox
    // -------------------------------------------------------------------------

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? SHAPE_ON : SHAPE_OFF;
    }

    // -------------------------------------------------------------------------
    // Rendering — invisible; all geometry is drawn by the BER
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
