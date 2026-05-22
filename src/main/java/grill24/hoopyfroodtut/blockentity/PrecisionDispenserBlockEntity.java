package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PrecisionDispenserBlockEntity extends DispenserBlockEntity {

    private static final Component DEFAULT_NAME = Component.translatable("container.precision_dispenser");

    public PrecisionDispenserBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.PRECISION_DISPENSER.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }
}
