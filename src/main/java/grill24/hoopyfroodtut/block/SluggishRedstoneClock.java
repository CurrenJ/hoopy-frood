package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.SluggishRedstoneClockBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class SluggishRedstoneClock extends RedstoneClock {

    public static final MapCodec<SluggishRedstoneClock> CODEC = simpleCodec(SluggishRedstoneClock::new);

    // Half-period in ticks for each delay level (full period = 2×): 4s, 10s, 30s, 2min
    public static final int[] SLUGGISH_HALF_PERIODS = {40, 100, 300, 1200};

    public SluggishRedstoneClock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<RedstoneClock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    public int[] getHalfPeriods() {
        return SLUGGISH_HALF_PERIODS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SluggishRedstoneClockBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.SLUGGISH_REDSTONE_CLOCK.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<SluggishRedstoneClockBlockEntity>) SluggishRedstoneClockBlockEntity::tick;
    }
}
