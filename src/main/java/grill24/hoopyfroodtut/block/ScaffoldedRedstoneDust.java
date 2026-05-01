package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.reflect.Field;
import java.util.Map;

public class ScaffoldedRedstoneDust extends RedStoneWireBlock {

    public static final MapCodec<ScaffoldedRedstoneDust> CODEC = simpleCodec(ScaffoldedRedstoneDust::new);

    // shouldSignal is a private reentrancy guard in RedStoneWireBlock that prevents the wire
    // from feeding back into its own power calculation. We need to silence BOTH the
    // ScaffoldedRedstoneDust singleton AND the vanilla RedStoneWireBlock singleton during our
    // getBlockSignal, because they are separate instances with independent flags. If only ours
    // is silenced, adjacent vanilla wires still report their full power (no −1 deduction),
    // creating a stable feedback loop that prevents power from draining.
    private static final Field SHOULD_SIGNAL_FIELD;

    // evaluator is private final in RedStoneWireBlock. We replace it with CrossTypeEvaluator so
    // that getIncomingWireSignal recognises vanilla wires as same-type neighbours (applies −1).
    private static final Field EVALUATOR_FIELD;

    static {
        try {
            SHOULD_SIGNAL_FIELD = RedStoneWireBlock.class.getDeclaredField("shouldSignal");
            SHOULD_SIGNAL_FIELD.setAccessible(true);
            EVALUATOR_FIELD = RedStoneWireBlock.class.getDeclaredField("evaluator");
            EVALUATOR_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to access RedStoneWireBlock fields", e);
        }
    }

    // Treats both ScaffoldedRedstoneDust and vanilla RedStoneWireBlock as the same wire type so
    // getIncomingWireSignal applies the standard −1 attenuation across both block types.
    private static class CrossTypeEvaluator extends DefaultRedstoneWireEvaluator {
        CrossTypeEvaluator(RedStoneWireBlock wireBlock) {
            super(wireBlock);
        }

        @Override
        protected int getWireSignal(BlockPos pos, BlockState state) {
            return state.getBlock() instanceof RedStoneWireBlock
                ? state.getValue(RedStoneWireBlock.POWER)
                : 0;
        }
    }

    public ScaffoldedRedstoneDust(Properties properties) {
        super(properties);
        try {
            EVALUATOR_FIELD.set(this, new CrossTypeEvaluator(this));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to install CrossTypeEvaluator", e);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<RedStoneWireBlock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    // Excludes UP: blocks sitting directly on top of scaffolded dust cannot power it.
    // Both shouldSignal flags are cleared so neither wire type feeds its stored power back
    // through getSignal; vanilla neighbours then contribute the correct power−1 value via
    // CrossTypeEvaluator.getIncomingWireSignal instead.
    @Override
    public int getBlockSignal(Level level, BlockPos pos) {
        RedStoneWireBlock vanillaWire = (RedStoneWireBlock) Blocks.REDSTONE_WIRE;
        try {
            SHOULD_SIGNAL_FIELD.setBoolean(this, false);
            SHOULD_SIGNAL_FIELD.setBoolean(vanillaWire, false);
            int best = 0;
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;
                int signal = level.getSignal(pos.relative(direction), direction);
                if (signal >= 15) return 15;
                if (signal > best) best = signal;
            }
            return best;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                SHOULD_SIGNAL_FIELD.setBoolean(this, true);
                SHOULD_SIGNAL_FIELD.setBoolean(vanillaWire, true);
            } catch (IllegalAccessException ignored) {}
        }
    }

    // ScaffoldedRedstoneDust is full-height, so UP connections (wire climbing a wall) are
    // geometrically invalid and produce broken blockstate/model combinations. Downgrade to SIDE.
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        return downgradeUpConnections(
            super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return downgradeUpConnections(super.getStateForPlacement(context));
    }

    private static BlockState downgradeUpConnections(BlockState state) {
        if (!(state.getBlock() instanceof RedStoneWireBlock)) return state;
        for (Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
            if (state.getValue(entry.getValue()) == RedstoneSide.UP) {
                state = state.setValue(entry.getValue(), RedstoneSide.SIDE);
            }
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
}
