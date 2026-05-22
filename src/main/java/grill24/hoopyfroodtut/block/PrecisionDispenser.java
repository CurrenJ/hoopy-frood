package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.PrecisionDispenserBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class PrecisionDispenser extends DispenserBlock {

    public static final MapCodec<PrecisionDispenser> CODEC = simpleCodec(PrecisionDispenser::new);

    // Set to true around each dispense call so mixins can zero out velocity variance.
    public static final ThreadLocal<Boolean> PRECISION_MODE = ThreadLocal.withInitial(() -> false);

    public PrecisionDispenser(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PrecisionDispenser> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrecisionDispenserBlockEntity(pos, state);
    }

    @Override
    protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
        PrecisionDispenserBlockEntity blockEntity = level.getBlockEntity(pos, HoopyFroodBlockEntityTypes.PRECISION_DISPENSER.get()).orElse(null);
        if (blockEntity == null) return;

        BlockSource source = new BlockSource(level, pos, state, blockEntity);
        int slot = blockEntity.getRandomSlot(level.getRandom());
        if (slot < 0) {
            level.levelEvent(1001, pos, 0);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(state));
        } else {
            ItemStack itemStack = blockEntity.getItem(slot);
            DispenseItemBehavior behavior = this.getDispenseMethod(level, itemStack);
            if (behavior != DispenseItemBehavior.NOOP) {
                PRECISION_MODE.set(true);
                try {
                    blockEntity.setItem(slot, behavior.dispense(source, itemStack));
                } finally {
                    PRECISION_MODE.remove();
                }
            }
        }
    }
}
