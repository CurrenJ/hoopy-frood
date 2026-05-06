package grill24.hoopyfroodtut.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A piston variant with a configurable push limit.
 * <p>
 * Extends {@link PistonBaseBlock} and overrides {@code checkIfExtend} and {@code moveBlocks}
 * to use {@link SturdyPistonStructureResolver} instead of {@link PistonStructureResolver},
 * reading the push limit from {@link Config#STURDY_PISTON_PUSH_LIMIT}.
 * <p>
 * Behaves identically to a vanilla piston in all other respects.
 */
public class SturdyPistonBaseBlock extends PistonBaseBlock {
    private final boolean isSticky;

    public SturdyPistonBaseBlock(boolean isSticky, Properties properties) {
        super(isSticky, properties);
        this.isSticky = isSticky;
    }

    @Override
    protected void checkIfExtend(Level level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(BlockStateProperties.FACING);
        boolean extend = getNeighborSignal(level, pos, direction);
        if (extend && !state.getValue(BlockStateProperties.EXTENDED)) {
            int limit = Config.STURDY_PISTON_PUSH_LIMIT.get();
            if (new SturdyPistonStructureResolver(level, pos, direction, true, limit).resolve()) {
                level.blockEvent(pos, this, 0, direction.get3DDataValue());
            }
        } else if (!extend && state.getValue(BlockStateProperties.EXTENDED)) {
            BlockPos pushedPos = pos.relative(direction, 2);
            BlockState pushedState = level.getBlockState(pushedPos);
            int event = 1;
            if (pushedState.is(Blocks.MOVING_PISTON)
                    && pushedState.getValue(BlockStateProperties.FACING) == direction
                    && level.getBlockEntity(pushedPos) instanceof PistonMovingBlockEntity pistonEntity
                    && pistonEntity.isExtending()
                    && (pistonEntity.getProgress(0.0F) < 0.5F
                    || level.getGameTime() == pistonEntity.getLastTicked()
                    || ((ServerLevel)level).isHandlingTick())) {
                event = 2;
            }
            level.blockEvent(pos, this, event, direction.get3DDataValue());
        }
    }

    @Override
    protected boolean moveBlocks(Level level, BlockPos pistonPos, Direction direction, boolean extending) {
        BlockPos armPos = pistonPos.relative(direction);
        if (!extending && (level.getBlockState(armPos).is(Blocks.PISTON_HEAD) || level.getBlockState(armPos).is(HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get()))) {
            level.setBlock(armPos, Blocks.AIR.defaultBlockState(), 276);
        }

        int limit = Config.STURDY_PISTON_PUSH_LIMIT.get();
        SturdyPistonStructureResolver resolver = new SturdyPistonStructureResolver(level, pistonPos, direction, extending, limit);
        if (!resolver.resolve()) {
            return false;
        } else {
            Map<BlockPos, BlockState> deleteAfterMove = Maps.newHashMap();
            List<BlockPos> toPush = resolver.getToPush();
            List<BlockState> toPushShapes = Lists.newArrayList();

            for (BlockPos pushPos : toPush) {
                BlockState blockState = level.getBlockState(pushPos);
                toPushShapes.add(blockState);
                deleteAfterMove.put(pushPos, blockState);
            }

            List<BlockPos> toDestroy = resolver.getToDestroy();
            BlockState[] toUpdate = new BlockState[toPush.size() + toDestroy.size()];
            Direction pushDirection = extending ? direction : direction.getOpposite();
            int updateIndex = 0;

            for (int i = toDestroy.size() - 1; i >= 0; i--) {
                BlockPos destroyPos = toDestroy.get(i);
                BlockState destroyState = level.getBlockState(destroyPos);
                BlockEntity blockEntity = destroyState.hasBlockEntity() ? level.getBlockEntity(destroyPos) : null;
                dropResources(destroyState, level, destroyPos, blockEntity);
                if (!destroyState.is(BlockTags.FIRE) && level.isClientSide()) {
                    level.levelEvent(2001, destroyPos, getId(destroyState));
                }
                destroyState.onDestroyedByPushReaction(level, destroyPos, direction, level.getFluidState(destroyPos));
                toUpdate[updateIndex++] = destroyState;
            }

            for (int i = toPush.size() - 1; i >= 0; i--) {
                BlockPos pushPos = toPush.get(i);
                BlockState blockState = level.getBlockState(pushPos);
                BlockPos targetPos = pushPos.relative(pushDirection);
                deleteAfterMove.remove(targetPos);
                BlockState movingState = Blocks.MOVING_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, direction);
                level.setBlock(targetPos, movingState, 324);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(targetPos, movingState, toPushShapes.get(i), direction, extending, false));
                toUpdate[updateIndex++] = blockState;
            }

            if (extending) {
                PistonType type = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState headState = HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get().defaultBlockState()
                        .setValue(PistonHeadBlock.FACING, direction)
                        .setValue(PistonHeadBlock.TYPE, type);
                BlockState movingPistonState = Blocks.MOVING_PISTON.defaultBlockState()
                        .setValue(BlockStateProperties.FACING, direction)
                        .setValue(MovingPistonBlock.TYPE, type);
                deleteAfterMove.remove(armPos);
                level.setBlock(armPos, movingPistonState, 324);
                level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(armPos, movingPistonState, headState, direction, true, true));
            }

            BlockState air = Blocks.AIR.defaultBlockState();

            for (BlockPos deletePos : deleteAfterMove.keySet()) {
                level.setBlock(deletePos, air, 82);
            }

            for (Entry<BlockPos, BlockState> entry : deleteAfterMove.entrySet()) {
                BlockPos deletePos = entry.getKey();
                BlockState oldState = entry.getValue();
                oldState.updateIndirectNeighbourShapes(level, deletePos, 2);
                air.updateNeighbourShapes(level, deletePos, 2);
                air.updateIndirectNeighbourShapes(level, deletePos, 2);
            }

            Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, resolver.getPushDirection(), null);
            updateIndex = 0;

            for (int i = toDestroy.size() - 1; i >= 0; i--) {
                BlockState state = toUpdate[updateIndex++];
                BlockPos destroyPos = toDestroy.get(i);
                if (level instanceof ServerLevel serverLevel) {
                    state.affectNeighborsAfterRemoval(serverLevel, destroyPos, false);
                }
                state.updateIndirectNeighbourShapes(level, destroyPos, 2);
                level.updateNeighborsAt(destroyPos, state.getBlock(), orientation);
            }

            for (BlockPos pushPos : toPush) {
                level.updateNeighborsAt(pushPos, toUpdate[updateIndex++].getBlock(), orientation);
            }

            if (extending) {
                level.updateNeighborsAt(armPos, HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get(), orientation);
            }

            return true;
        }
    }

    /**
     * Copy of {@link PistonBaseBlock#getNeighborSignal(SignalGetter, BlockPos, Direction)}.
     */
    private static boolean getNeighborSignal(SignalGetter level, BlockPos pos, Direction pushDirection) {
        for (Direction dir : Direction.values()) {
            if (dir != pushDirection && level.hasSignal(pos.relative(dir), dir)) {
                return true;
            }
        }
        if (level.hasSignal(pos, Direction.DOWN)) {
            return true;
        } else {
            BlockPos above = pos.above();
            for (Direction dir : Direction.values()) {
                if (dir != Direction.DOWN && level.hasSignal(above.relative(dir), dir)) {
                    return true;
                }
            }
            return false;
        }
    }
}
