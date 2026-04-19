package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SluggishPulseLatchBlockEntity extends PulseLatchBlockEntity {

    public SluggishPulseLatchBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.SLUGGISH_PULSE_LATCH.get(), pos, state);
    }
}
