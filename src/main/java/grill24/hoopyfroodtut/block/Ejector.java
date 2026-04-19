package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.EjectorBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class Ejector extends DispenserBlock {

    public static final MapCodec<Ejector> CODEC = simpleCodec(Ejector::new);

    private static final int EJECT_INTERVAL = 4;

    public Ejector(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<Ejector> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EjectorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.EJECTOR.get(), EjectorBlockEntity::tick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   net.minecraft.world.level.block.Block block,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        // No redstone interaction — ejector always fires while it has items.
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Suppress vanilla dispenser scheduled tick.
    }

    @Override
    protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
        EjectorBlockEntity blockEntity = level.getBlockEntity(pos, HoopyFroodBlockEntityTypes.EJECTOR.get()).orElse(null);
        if (blockEntity == null) return;

        BlockSource source = new BlockSource(level, pos, state, blockEntity);
        int slot = blockEntity.getRandomSlot(level.getRandom());
        if (slot < 0) {
            level.levelEvent(1001, pos, 0);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(blockEntity.getBlockState()));
        } else {
            ItemStack itemStack = blockEntity.getItem(slot);
            DispenseItemBehavior behavior = this.getDispenseMethod(level, itemStack);
            if (behavior != DispenseItemBehavior.NOOP) {
                blockEntity.setItem(slot, behavior.dispense(source, itemStack));
            }
        }
    }

    public static void ejectTick(Level level, BlockPos pos, BlockState state, EjectorBlockEntity blockEntity) {
        if (level.getGameTime() % EJECT_INTERVAL != 0) return;
        if (blockEntity.isEmpty()) return;
        ((Ejector) state.getBlock()).dispenseFrom((ServerLevel) level, state, pos);
    }
}
