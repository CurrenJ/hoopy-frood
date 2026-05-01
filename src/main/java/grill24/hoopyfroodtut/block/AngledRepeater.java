package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base for angled repeaters. Reads signal from one lateral side and
 * outputs in the perpendicular direction (FACING). Chirality (left/right) is
 * determined by subclass implementations of {@link #getInputDirection(BlockState)}.
 */
public abstract class AngledRepeater extends DiodeBlock {

    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    public AngledRepeater(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(DELAY, 1)
                .setValue(LOCKED, false)
                .setValue(POWERED, false));
    }

    /**
     * Returns the direction from which this angled repeater reads its input.
     * Left variant → FACING.getClockWise(), Right variant → FACING.getCounterClockWise().
     */
    public abstract Direction getInputDirection(BlockState state);

    /**
     * Returns the FACING value to store when placed by a player looking in playerDir.
     * Left variant → playerDir.getClockWise(), Right variant → playerDir.getCounterClockWise().
     * DiodeBlock.getStateForPlacement uses playerDir.getOpposite() (vanilla repeater back-to-input),
     * but angled repeaters need a 90-degree rotation so input comes from behind the player.
     */
    protected abstract Direction getPlacementFacing(Direction playerDir);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, DELAY, LOCKED, POWERED);
    }

    @Override
    protected int getDelay(BlockState state) {
        return state.getValue(DELAY) * 2;
    }

    @Override
    protected int getInputSignal(Level level, BlockPos pos, BlockState state) {
        Direction inputDir = getInputDirection(state);
        BlockPos targetPos = pos.relative(inputDir);
        int input = level.getSignal(targetPos, inputDir);
        if (input >= 15) return input;
        BlockState targetBlockState = level.getBlockState(targetPos);
        return Math.max(input,
                targetBlockState.getBlock() instanceof RedStoneWireBlock
                        ? targetBlockState.getValue(RedStoneWireBlock.POWER)
                        : 0);
    }

    @Override
    protected int getAlternateSignal(SignalGetter level, BlockPos pos, BlockState state) {
        // Lock sources: the two faces that are neither input nor output.
        // Output reaches the block at pos.relative(FACING.getOpposite()); input is at pos.relative(inputDir).
        // The remaining neutral sides are FACING itself and inputDir.getOpposite().
        Direction facing = state.getValue(FACING);
        Direction inputDir = getInputDirection(state);
        Direction lock1 = facing;
        Direction lock2 = inputDir.getOpposite();
        return Math.max(
                level.getControlInputSignal(pos.relative(lock1), lock1, true),
                level.getControlInputSignal(pos.relative(lock2), lock2, true)
        );
    }

    @Override
    public boolean isLocked(LevelReader level, BlockPos pos, BlockState state) {
        return this.getAlternateSignal(level, pos, state) > 0;
    }

    @Override
    protected boolean sideInputDiodesOnly() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction dir) {
        // canConnectRedstone is queried with dir = direction FROM the neighbor wire TO this block.
        // Output: the block receiving our signal is at pos.relative(FACING.getOpposite()); it queries with dir==FACING.
        // Input: the block providing signal is at pos.relative(inputDir); it queries with dir==inputDir.getOpposite().
        return dir == state.getValue(FACING) || dir == getInputDirection(state).getOpposite();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction playerDir = context.getHorizontalDirection();
        BlockState state = this.defaultBlockState().setValue(FACING, getPlacementFacing(playerDir));
        return state.setValue(LOCKED, this.isLocked(context.getLevel(), context.getClickedPos(), state));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (directionToNeighbour == Direction.DOWN && !this.canSurviveOn(level, neighbourPos, neighbourState)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        // Update locked state on lateral neighbour changes
        Direction facing = state.getValue(FACING);
        Direction inputDir = getInputDirection(state);
        Direction lockAxis1 = facing;
        Direction lockAxis2 = inputDir.getOpposite();
        if (!level.isClientSide()
                && (directionToNeighbour == lockAxis1 || directionToNeighbour == lockAxis2)) {
            return state.setValue(LOCKED, this.isLocked(level, pos, state));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        level.setBlock(pos, state.cycle(DELAY), 3);
        return InteractionResult.SUCCESS;
    }
}
