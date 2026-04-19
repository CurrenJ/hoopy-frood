package grill24.hoopyfroodtut.block;

import grill24.hoopyfroodtut.blockentity.ExpellerBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class Expeller extends DropperBlock {

    private static final DispenseItemBehavior DROP_BEHAVIOUR = new DefaultDispenseItemBehavior();
    private static final int EXPEL_INTERVAL = 4;

    public Expeller(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExpellerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HoopyFroodBlockEntityTypes.EXPELLER.get(), ExpellerBlockEntity::tick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   net.minecraft.world.level.block.Block block,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        // No redstone interaction — expeller always fires while it has items.
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Suppress vanilla dropper scheduled tick.
    }

    @Override
    protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
        ExpellerBlockEntity blockEntity = level.getBlockEntity(pos, HoopyFroodBlockEntityTypes.EXPELLER.get()).orElse(null);
        if (blockEntity == null) return;

        BlockSource source = new BlockSource(level, pos, state, blockEntity);
        int slot = blockEntity.getRandomSlot(level.getRandom());
        if (slot < 0) {
            level.levelEvent(1001, pos, 0);
        } else {
            ItemStack itemStack = blockEntity.getItem(slot);
            if (!itemStack.isEmpty()) {
                Direction direction = state.getValue(FACING);
                var into = HopperBlockEntity.getContainerOrHandlerAt(level, pos.relative(direction), direction.getOpposite());
                ItemStack remaining;
                if (into.isEmpty()) {
                    remaining = DROP_BEHAVIOUR.dispense(source, itemStack);
                } else {
                    if (into.container() != null) {
                        remaining = HopperBlockEntity.addItem(blockEntity, into.container(), itemStack.copyWithCount(1), direction.getOpposite());
                    } else {
                        remaining = net.neoforged.neoforge.transfer.item.ItemUtil.insertItemReturnRemaining(into.itemHandler(), itemStack.copyWithCount(1), false, null);
                    }
                    if (remaining.isEmpty()) {
                        remaining = itemStack.copy();
                        remaining.shrink(1);
                    } else {
                        remaining = itemStack.copy();
                    }
                }
                blockEntity.setItem(slot, remaining);
            }
        }
    }

    public static void expelTick(Level level, BlockPos pos, BlockState state, ExpellerBlockEntity blockEntity) {
        if (level.getGameTime() % EXPEL_INTERVAL != 0) return;
        if (blockEntity.isEmpty()) return;
        ((Expeller) state.getBlock()).dispenseFrom((ServerLevel) level, state, pos);
    }
}
