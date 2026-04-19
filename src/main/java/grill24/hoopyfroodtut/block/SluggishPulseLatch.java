package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.SluggishPulseLatchBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SluggishPulseLatch extends PulseLatch {

    public static final MapCodec<SluggishPulseLatch> CODEC = simpleCodec(SluggishPulseLatch::new);

    // Hold duration in ticks for each delay level (1-4): 2s, 5s, 15s, 60s
    public static final int[] SLUGGISH_DURATIONS = {40, 100, 300, 1200};

    public SluggishPulseLatch(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends PulseLatch> codec() {
        return CODEC;
    }

    @Override
    protected int[] getDurations() {
        return SLUGGISH_DURATIONS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SluggishPulseLatchBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.SLUGGISH_PULSE_LATCH.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<SluggishPulseLatchBlockEntity>)
                SluggishPulseLatchBlockEntity::tick;
    }
}
