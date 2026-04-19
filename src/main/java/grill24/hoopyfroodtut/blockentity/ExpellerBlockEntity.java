package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.Expeller;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ExpellerBlockEntity extends DispenserBlockEntity {

    private static final Component DEFAULT_NAME = Component.translatable("container.expeller");

    public ExpellerBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.EXPELLER.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ExpellerBlockEntity blockEntity) {
        Expeller.expelTick(level, pos, state, blockEntity);
    }
}
