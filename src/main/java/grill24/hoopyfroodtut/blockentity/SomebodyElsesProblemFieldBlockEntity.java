package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodSounds;
import grill24.hoopyfroodtut.core.SepFieldManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for the Somebody Else's Problem Field.
 * <p>
 * Handles field registration with {@link SepFieldManager} and tracks the
 * redstone-driven active state used by the client-side BER for animation.
 */
public class SomebodyElsesProblemFieldBlockEntity extends BlockEntity {

    private boolean isActive = false;
    /** Game-tick timestamp of the last active-state toggle, or -1 if never toggled. */
    private float lastToggleTime = -1f;

    public SomebodyElsesProblemFieldBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.SOMEBODY_ELSES_PROBLEM_FIELD.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Active-state management (called from the block on redstone changes)
    // -------------------------------------------------------------------------

    public void setActive(boolean active) {
        if (this.isActive == active) return;
        this.isActive = active;
        if (level != null) {
            this.lastToggleTime = level.getGameTime();
            if (!level.isClientSide()) {
                if (active) {
                    SepFieldManager.register(level, worldPosition);
                    level.playSound(null, worldPosition, HoopyFroodSounds.SEP_FIELD_ACTIVATE.get(),
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                } else {
                    SepFieldManager.unregister(level, worldPosition);
                    level.playSound(null, worldPosition, HoopyFroodSounds.SEP_FIELD_DEACTIVATE.get(),
                            SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean isActive() { return isActive; }
    public float getLastToggleTime() { return lastToggleTime; }

    // -------------------------------------------------------------------------
    // Registration lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onLoad() {
        super.onLoad();
        // Only register with the manager if the field is currently powered.
        if (level != null && !level.isClientSide() && isActive) {
            SepFieldManager.register(level, worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            SepFieldManager.unregister(level, worldPosition);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            SepFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isActive = input.getBooleanOr("IsActive", false);
        this.lastToggleTime = input.getFloatOr("LastToggleTime", -1f);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("IsActive", isActive);
        output.putFloat("LastToggleTime", lastToggleTime);
    }

    // -------------------------------------------------------------------------
    // Networking — full state sync so the client BER can animate correctly
    // -------------------------------------------------------------------------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
