package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.ScaffoldedComparatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public class ScaffoldedComparator extends ComparatorBlock {

    public static final MapCodec<ScaffoldedComparator> CODEC = simpleCodec(ScaffoldedComparator::new);

    public ScaffoldedComparator(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<ComparatorBlock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScaffoldedComparatorBlockEntity(pos, state);
    }

    // -------------------------------------------------------------------------
    // Override methods that read/write ComparatorBlockEntity to use our BE type.
    // -------------------------------------------------------------------------

    private int getStoredOutput(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ScaffoldedComparatorBlockEntity scbe ? scbe.getOutputSignal() : 0;
    }

    private void setStoredOutput(Level level, BlockPos pos, int value) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ScaffoldedComparatorBlockEntity scbe) {
            scbe.setOutputSignal(value);
        }
    }

    private int calculateOutputSignal(Level level, BlockPos pos, BlockState state) {
        int inputSignal = this.getInputSignal(level, pos, state);
        if (inputSignal == 0) return 0;
        int alternateSignal = this.getAlternateSignal(level, pos, state);
        if (alternateSignal > inputSignal) return 0;
        return state.getValue(MODE) == ComparatorMode.SUBTRACT ? inputSignal - alternateSignal : inputSignal;
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state) {
        int outputValue = this.calculateOutputSignal(level, pos, state);
        int oldValue = getStoredOutput(level, pos);

        if (oldValue != outputValue || state.getValue(MODE) == ComparatorMode.COMPARE) {
            setStoredOutput(level, pos, outputValue);

            boolean sourceOn = this.shouldTurnOn(level, pos, state);
            boolean isOn = state.getValue(POWERED);
            if (isOn && !sourceOn) {
                level.setBlock(pos, state.setValue(POWERED, false), 2);
            } else if (!isOn && sourceOn) {
                level.setBlock(pos, state.setValue(POWERED, true), 2);
            }

            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof ScaffoldedComparatorBlockEntity scbe ? scbe.getOutputSignal() : 0;
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            int outputValue = this.calculateOutputSignal(level, pos, state);
            int oldValue = getStoredOutput(level, pos);
            if (outputValue != oldValue || state.getValue(POWERED) != this.shouldTurnOn(level, pos, state)) {
                TickPriority priority = this.shouldPrioritize(level, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
                level.scheduleTick(pos, this, 2, priority);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.refreshOutputState(level, pos, state);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {
        BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(b0, b1);
    }
}
