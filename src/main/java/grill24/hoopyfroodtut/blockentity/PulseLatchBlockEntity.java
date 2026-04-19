package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.PulseLatch;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PulseLatchBlockEntity extends BlockEntity {

    public boolean wasReceivingPower = false;
    private int ticksRemaining = 0;

    public PulseLatchBlockEntity(BlockPos pos, BlockState state) {
        this(HoopyFroodBlockEntityTypes.PULSE_LATCH.get(), pos, state);
    }

    protected PulseLatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        wasReceivingPower = input.getBooleanOr("was_receiving_power", false);
        ticksRemaining    = input.getIntOr("ticks_remaining", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("was_receiving_power", wasReceivingPower);
        output.putInt("ticks_remaining", ticksRemaining);
    }

    public void trigger(Level level, BlockPos pos, BlockState state, int duration) {
        ticksRemaining = duration;
        if (!state.getValue(PulseLatch.POWERED)) {
            level.setBlock(pos, state.setValue(PulseLatch.POWERED, true), 3);
        }
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PulseLatchBlockEntity be) {
        if (be.ticksRemaining <= 0) return;
        be.ticksRemaining--;
        if (be.ticksRemaining == 0 && state.getValue(PulseLatch.POWERED)) {
            level.setBlock(pos, state.setValue(PulseLatch.POWERED, false), 3);
        }
        be.setChanged();
    }
}
