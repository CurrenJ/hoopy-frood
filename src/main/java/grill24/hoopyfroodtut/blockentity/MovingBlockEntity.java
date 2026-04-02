package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Abstract base for block entities that can "move" block-by-block in any direction,
 * using the smooth offset-before-setblock animation illusion.
 * <p>
 * Subclasses initiate a move by calling {@link #startAdvance(Level, Direction)}, which
 * records the game time and move direction, then notifies clients. The renderer reads
 * {@link #getForwardOffset(long, float)} (0→1) to slide the model visually before the
 * block actually relocates. Once {@link #ADVANCE_FORWARD_DURATION} ticks elapse, the
 * subclass is responsible for placing itself at the new position and destroying the old one.
 */
public abstract class MovingBlockEntity extends BlockEntity {

    /** Duration of the smooth advance animation in ticks. */

    /**
     * Game-tick timestamp when the current advance animation started, or {@code -1} when idle.
     * Protected so subclass tick logic can read it directly without a call overhead.
     */
    protected int advanceTimestamp = -1;

    /** World-space direction the block is currently (or was last) moving toward. */
    protected Direction moveDirection = Direction.NORTH;

    public MovingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // -------------------------------------------------------------------------
    // Movement helpers
    // -------------------------------------------------------------------------

    /**
     * Starts the advance animation toward {@code direction}:
     * stores the current game time and direction, marks the BE dirty,
     * and broadcasts a block update so clients start the visual offset.
     */
    protected void startAdvance(Level level, Direction direction) {
        this.moveDirection = direction;
        this.advanceTimestamp = (int) level.getGameTime();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    /**
     * Returns the eased 0→1 forward progress for the current frame.
     * Returns {@code 0} when no animation is active.
     *
     * @param gameTime   current server game time (from {@code Level.getGameTime()})
     * @param partialTick sub-tick interpolation value
     */
    public float getForwardOffset(long gameTime, float partialTick) {
        if (advanceTimestamp < 0) return 0f;
        float elapsed = (gameTime - advanceTimestamp) + partialTick;
        float t = Math.min(1f, elapsed / getAdvanceForwardDuration());
        return Util.easeInOutCubic(t);
    }

    public int getAdvanceTimestamp() { return advanceTimestamp; }
    public Direction getMoveDirection() { return moveDirection; }

    public abstract int getAdvanceForwardDuration();

    /**
     * Returns a VoxelShape (1×1×1 cube) translated by the current animation offset.
     * Pass {@code partialTick = 0} since shape queries have no sub-tick interpolation.
     */
    public static VoxelShape animatedShape(long gameTime, MovingBlockEntity be) {
        float offset = be.getForwardOffset(gameTime, 0f);
        if (offset == 0f) return Shapes.block();
        Direction dir = be.getMoveDirection();
        double dx = dir.getStepX() * offset;
        double dy = dir.getStepY() * offset;
        double dz = dir.getStepZ() * offset;
        return Shapes.create(new AABB(dx, dy, dz, 1 + dx, 1 + dy, 1 + dz));
    }

    // -------------------------------------------------------------------------
    // Persistence & networking
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.advanceTimestamp = input.getIntOr("AdvanceTimestamp", -1);
        String dirName = input.getStringOr("MoveDirection", "north");
        Direction parsed = Direction.byName(dirName);
        this.moveDirection = parsed != null ? parsed : Direction.NORTH;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("AdvanceTimestamp", advanceTimestamp);
        output.putString("MoveDirection", moveDirection.getName());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
