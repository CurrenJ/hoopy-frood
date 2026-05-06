package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.ProximitySensorBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ProximitySensor extends BaseEntityBlock {

    public static final MapCodec<ProximitySensor> CODEC = simpleCodec(ProximitySensor::new);
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 6.0);

    // Cycle of configurable radii in blocks
    public static final int[] RADII = {4, 8, 16, 32, 48, 64, 72, 96, 112, 128};

    public ProximitySensor(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    public MapCodec<ProximitySensor> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ProximitySensorBlockEntity be) {
            return be.currentSignal;
        }
        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ProximitySensorBlockEntity be) {
            return be.currentSignal;
        }
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) return InteractionResult.PASS;
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ProximitySensorBlockEntity be) {
            if (player.isShiftKeyDown()) {
                be.playersOnly = !be.playersOnly;
                be.setChanged();
                if (player instanceof ServerPlayer sp)
                    sp.sendSystemMessage(Component.literal("Targets: " + (be.playersOnly ? "players" : "all mobs")), true);
            } else {
                int idx = 0;
                for (int i = 0; i < RADII.length; i++) {
                    if (RADII[i] == be.radius) { idx = (i + 1) % RADII.length; break; }
                }
                be.radius = RADII[idx];
                be.setChanged();
                if (player instanceof ServerPlayer sp)
                    sp.sendSystemMessage(Component.literal("Radius: " + be.radius + " blocks"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ProximitySensorBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.PROXIMITY_SENSOR.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ProximitySensorBlockEntity>) ProximitySensorBlockEntity::tick;
    }
}
