package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.SluggishReleaseLatchBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SluggishReleaseLatch extends ReleaseLatch {

    public static final MapCodec<SluggishReleaseLatch> CODEC = simpleCodec(SluggishReleaseLatch::new);

    // Hold duration in ticks after input falls before output goes LOW: 2s, 5s, 15s, 60s
    public static final int[] SLUGGISH_DURATIONS = {40, 100, 300, 1200};

    public SluggishReleaseLatch(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends ReleaseLatch> codec() {
        return CODEC;
    }

    @Override
    protected int[] getDurations() {
        return SLUGGISH_DURATIONS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SluggishReleaseLatchBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.SLUGGISH_RELEASE_LATCH.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<SluggishReleaseLatchBlockEntity>) SluggishReleaseLatchBlockEntity::tick;
    }
}
