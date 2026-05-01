package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SluggishRedstoneClockBlockEntity extends RedstoneClockBlockEntity {

    public SluggishRedstoneClockBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.SLUGGISH_REDSTONE_CLOCK.get(), pos, state);
    }
}
