package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.ReleaseLatchBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
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

public class ReleaseLatch extends DiodeBlock implements EntityBlock {

    public static final MapCodec<ReleaseLatch> CODEC = simpleCodec(ReleaseLatch::new);
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

    // Hold duration in ticks after input falls before output goes LOW
    public static final int[] DURATIONS = {2, 4, 6, 8};

    protected int[] getDurations() { return DURATIONS; }

    public ReleaseLatch(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(DELAY, 1));
    }

    @Override
    public MapCodec<? extends ReleaseLatch> codec() {
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
            level.playSound(null, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, 0.5f + (delay - 1) * 0.1f);
            if (player instanceof ServerPlayer sp)
                sp.sendSystemMessage(Component.literal("Release delay: " + getDurations()[delay - 1] + " ticks"), true);
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
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReleaseLatchBlockEntity be) {
            boolean receivingNow = this.shouldTurnOn(level, pos, state);
            if (receivingNow && !be.wasReceivingPower) {
                be.onInputRise(level, pos, state);
            } else if (!receivingNow && be.wasReceivingPower) {
                be.onInputFall(level, pos, state, getDurations()[state.getValue(DELAY) - 1]);
            }
            be.wasReceivingPower = receivingNow;
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Suppress DiodeBlock scheduled-tick; release countdown is handled by BE ticker.
    }

    // Initialize wasReceivingPower on placement; if already powered, immediately raise output.
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReleaseLatchBlockEntity be) {
            boolean receivingNow = this.shouldTurnOn(level, pos, state);
            be.wasReceivingPower = receivingNow;
            if (receivingNow && !state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, true), 3);
            }
        }
    }

    @Override
    public boolean canConnectRedstone(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        Direction facing = state.getValue(FACING);
        return direction == facing || direction == facing.getOpposite();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReleaseLatchBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.RELEASE_LATCH.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ReleaseLatchBlockEntity>) ReleaseLatchBlockEntity::tick;
    }
}
