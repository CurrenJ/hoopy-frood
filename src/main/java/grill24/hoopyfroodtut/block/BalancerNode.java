package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.item.BalancerRangeExtender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The Balancer Node is a small directional block that attaches to any face of a block.
 * <p>
 * FACING = the outward direction the node protrudes (same convention as the observer block).
 * The attached/source block is always in the FACING.getOpposite() direction.
 * Distribution targets extend in the FACING direction.
 * <p>
 * Layout: [SOURCE BLOCK] → [BALANCER NODE facing EAST] → [DEST1] [DEST2] [DEST3] ...
 * <p>
 * Player interactions:
 * <ul>
 *   <li>Right-click with a {@link BalancerRangeExtender} — inserts the extender, consuming one
 *       from the stack and increasing the effective scan range.</li>
 *   <li>Shift + right-click with empty hand — spawns particles that preview the recognised
 *       source and all potential sinks within range.</li>
 * </ul>
 */
public class BalancerNode extends BaseEntityBlock {
    MapCodec<BalancerNode> CODEC = simpleCodec(BalancerNode::new);

    /** Direction the node faces outward, away from the surface it's attached to. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    /** Whether the node is currently active (successfully transferring items). */
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        SHAPES.put(Direction.NORTH, Block.box(4, 4, 12, 12, 12, 16));
        SHAPES.put(Direction.SOUTH, Block.box(4, 4,  0, 12, 12,  4));
        SHAPES.put(Direction.EAST,  Block.box( 0, 4, 4,  4, 12, 12));
        SHAPES.put(Direction.WEST,  Block.box(12, 4, 4, 16, 12, 12));
        SHAPES.put(Direction.UP,    Block.box(4,  0, 4, 12,  4, 12));
        SHAPES.put(Direction.DOWN,  Block.box(4, 12, 4, 12, 16, 12));
    }

    public BalancerNode(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    // -------------------------------------------------------------------------
    // Placement and Shape
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos nodePos = context.getClickedPos();
        BlockState candidate = this.defaultBlockState().setValue(FACING, facing).setValue(POWERED, false);
        return candidate.canSurvive(context.getLevel(), nodePos) ? candidate : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(facing.getOpposite());
        return !level.getBlockState(attachedPos).isAir();
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    // -------------------------------------------------------------------------
    // Player interaction
    // -------------------------------------------------------------------------

    /**
     * Right-clicking with a {@link BalancerRangeExtender} inserts it into the node,
     * increasing the effective scan range by {@link BalancerNodeBlockEntity#RANGE_PER_EXTENDER} blocks.
     */
    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof BalancerRangeExtender)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof BalancerNodeBlockEntity be) {
            be.addExtender();
            if (!player.isCreative()) stack.shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Shift + right-click with empty hand spawns a particle preview of the source
     * and all recognised / potential sinks within range.
     */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof BalancerNodeBlockEntity be) {
            be.spawnPreviewParticles((ServerLevel) level, pos, state);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    // -------------------------------------------------------------------------
    // Drops — include stored range extenders
    // -------------------------------------------------------------------------

    /** Drops extenders when broken in creative mode. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide() && player.getAbilities().instabuild
                && level.getBlockEntity(pos) instanceof BalancerNodeBlockEntity be) {
            be.dropExtenders(level, pos);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, toolStack, willHarvest, fluid);
    }

    /** Appends stored Balancer Range Extenders to the normal loot-table drops. */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof BalancerNodeBlockEntity node) {
            for (int i = 0; i < node.getStoredExtenders(); i++) {
                drops.add(new ItemStack(HoopyFroodItems.BALANCER_RANGE_EXTENDER.get()));
            }
        }
        return drops;
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BalancerNodeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.BALANCER_NODE.get(),
                BalancerNodeBlockEntity::tick);
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
