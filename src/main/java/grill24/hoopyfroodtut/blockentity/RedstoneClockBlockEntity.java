package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.RedstoneClock;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RedstoneClockBlockEntity extends BlockEntity {

    public boolean running = false;
    public int tickCount = 0;

    public RedstoneClockBlockEntity(BlockPos pos, BlockState state) {
        this(HoopyFroodBlockEntityTypes.REDSTONE_CLOCK.get(), pos, state);
    }

    protected RedstoneClockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        running   = input.getBooleanOr("running", false);
        tickCount = input.getIntOr("tick_count", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("running", running);
        output.putInt("tick_count", tickCount);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RedstoneClockBlockEntity be) {
        if (!be.running) return;

        int halfPeriod = ((RedstoneClock) state.getBlock()).getHalfPeriods()[state.getValue(RedstoneClock.DELAY) - 1];
        be.tickCount++;
        if (be.tickCount >= halfPeriod) {
            be.tickCount = 0;
            boolean newPowered = !state.getValue(RedstoneClock.POWERED);
            level.setBlock(pos, state.setValue(RedstoneClock.POWERED, newPowered), 3);
        }
        be.setChanged();
    }
}
