package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.RedstoneClockBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class RedstoneClock extends DiodeBlock implements EntityBlock {

    public static final MapCodec<RedstoneClock> CODEC = simpleCodec(RedstoneClock::new);
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    // Half-period in ticks for each delay level (full period = 2×)
    public static final int[] HALF_PERIODS = {2, 5, 10, 20};

    public int[] getHalfPeriods() { return HALF_PERIODS; }

    public RedstoneClock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(DELAY, 1));
    }

    @Override
    public MapCodec<RedstoneClock> codec() {
        return CODEC;
    }

    @Override
    protected int getDelay(BlockState state) {
        return 2;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, POWERED, DELAY);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            BlockState newState = state.cycle(DELAY);
            level.setBlock(pos, newState, 3);
            int delay = newState.getValue(DELAY);
            int halfPeriod = getHalfPeriods()[delay - 1];
            level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, 0.5f + (delay - 1) * 0.1f);
            if (player instanceof ServerPlayer sp)
                sp.sendSystemMessage(Component.literal("Period: " + (halfPeriod * 2) + " ticks"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            dropResources(state, level, pos, be);
            level.removeBlock(pos, false);
            for (Direction dir : Direction.values()) level.updateNeighborsAt(pos.relative(dir), this);
            return;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RedstoneClockBlockEntity be) {
            boolean powered = this.shouldTurnOn(level, pos, state);
            if (!powered && be.running) {
                be.running = false;
                be.tickCount = 0;
                if (state.getValue(POWERED))
                    level.setBlock(pos, state.setValue(POWERED, false), 3);
            } else if (powered && !be.running) {
                be.running = true;
            }
            be.setChanged();
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Suppress DiodeBlock scheduled-tick behavior; oscillation is handled by BE ticker.
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RedstoneClockBlockEntity be) {
            be.running = this.shouldTurnOn(level, pos, state);
            be.setChanged();
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        Direction facing = state.getValue(FACING);
        return direction == facing || direction == facing.getOpposite();
    }

    public boolean isReceivingInput(Level level, BlockPos pos, BlockState state) {
        return this.shouldTurnOn(level, pos, state);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneClockBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.REDSTONE_CLOCK.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<RedstoneClockBlockEntity>) RedstoneClockBlockEntity::tick;
    }
}
