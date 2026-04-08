package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Stores a single item on an animated fluid surface.
 * <p>
 * Server tick checks whether any player other than the depositer is within
 * {@link #PRIVACY_RADIUS} blocks. When such a player is detected, {@code isPrivate}
 * is set to {@code true}, which instructs the renderer to hide the item and show a
 * disabled visual state.
 */
public class PersonalPrivateItemPresenterBlockEntity extends BlockEntity {

    public static final double PRIVACY_RADIUS = 8.0;
    private static final int PRIVACY_CHECK_INTERVAL = 10; // ticks between scans

    /** Block-local Y of the fluid surface — mirrors DEFAULT_HEIGHT in the renderer.
     *  Read by {@link grill24.hoopyfroodtut.mixin.ItemEntityMixin} to determine the float target. */
    public static final float FLUID_SURFACE_Y = 12.0f / 16.0f;

    // ── Persistent fields ──────────────────────────────────────────────────

    private ItemStack storedItem = ItemStack.EMPTY;
    @Nullable private UUID depositerUUID = null;

    /** Server-computed flag; synced to client every time it changes. */
    private boolean isPrivate = false;

    // Display configuration (stored in NBT, adjustable per-block)
    private int gridSize = 12;
    private float waveAmp = 1.0f;
    private float waveSpeed = 1.0f;
    private float bobAmp = 0.08f;
    private float bobSpeed = 1.0f;
    private int surfaceColor = 0xFFFFFFFF;
    private boolean spinItem = false;
    private boolean voxelMode = false;
    private boolean physicsEnabled = false;
    private SurfaceParticleType surfaceParticleType = SurfaceParticleType.NONE;

    // ── Surface particle type ──────────────────────────────────────────────

    public enum SurfaceParticleType {
        NONE, PINK_PETALS, LEAF_LITTER;

        public String toSerializedName() {
            return switch (this) {
                case PINK_PETALS -> "pink_petals";
                case LEAF_LITTER -> "leaf_litter";
                default          -> "none";
            };
        }

        public static SurfaceParticleType fromString(String s) {
            return switch (s) {
                case "pink_petals" -> PINK_PETALS;
                case "leaf_litter" -> LEAF_LITTER;
                default            -> NONE;
            };
        }
    }

    // ── Constructor ────────────────────────────────────────────────────────

    public PersonalPrivateItemPresenterBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.PERSONAL_PRIVATE_ITEM_PRESENTER.get(), pos, state);
    }

    // ── Server tick ────────────────────────────────────────────────────────

    public static void tick(Level level, BlockPos pos, BlockState state,
                             PersonalPrivateItemPresenterBlockEntity be) {
        if (level.isClientSide()) return;
        if (level.getGameTime() % PRIVACY_CHECK_INTERVAL != 0) return;

        boolean nowPrivate = false;

        // Only enforce privacy when an item is stored and we know who deposited it.
        if (!be.storedItem.isEmpty() && be.depositerUUID != null) {
            AABB scanBox = new AABB(pos).inflate(PRIVACY_RADIUS);
            List<Player> nearby = level.getEntitiesOfClass(Player.class, scanBox);
            for (Player player : nearby) {
                if (!player.getUUID().equals(be.depositerUUID)) {
                    nowPrivate = true;
                    break;
                }
            }
        }

        if (nowPrivate != be.isPrivate) {
            be.isPrivate = nowPrivate;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public ItemStack getStoredItem() {
        return storedItem;
    }

    /**
     * Store a new item, recording the UUID of the player who deposited it.
     * Pass {@code null} for {@code depositer} when clearing the slot.
     */
    public void setStoredItem(ItemStack stack, @Nullable UUID depositer) {
        this.storedItem = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        this.depositerUUID = stack.isEmpty() ? null : depositer;
        if (stack.isEmpty()) this.isPrivate = false;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean isPrivate() { return isPrivate; }

    public int getGridSize()    { return gridSize; }
    public float getWaveAmp()   { return waveAmp; }
    public float getWaveSpeed() { return waveSpeed; }
    public float getBobAmp()    { return bobAmp; }
    public float getBobSpeed()  { return bobSpeed; }
    public int getSurfaceColor(){ return surfaceColor; }
    public boolean isSpinItem()        { return spinItem; }
    public boolean isVoxelMode()       { return voxelMode; }
    public boolean isPhysicsEnabled()  { return physicsEnabled; }
    public SurfaceParticleType getSurfaceParticleType() { return surfaceParticleType; }

    // ── Setters (sync on set) ──────────────────────────────────────────────

    public void setGridSize(int v)       { this.gridSize = Math.max(1, Math.min(32, v)); markChangedAndSync(); }
    public void setWaveAmp(float v)      { this.waveAmp = v;       markChangedAndSync(); }
    public void setWaveSpeed(float v)    { this.waveSpeed = v;     markChangedAndSync(); }
    public void setBobAmp(float v)       { this.bobAmp = v;        markChangedAndSync(); }
    public void setBobSpeed(float v)     { this.bobSpeed = v;      markChangedAndSync(); }
    public void setSurfaceColor(int v)   { this.surfaceColor = v;  markChangedAndSync(); }
    public void setSpinItem(boolean v)   { this.spinItem = v;      markChangedAndSync(); }
    public void setVoxelMode(boolean v)  { this.voxelMode = v;     markChangedAndSync(); }
    public void setPhysicsEnabled(boolean v) { this.physicsEnabled = v; markChangedAndSync(); }
    public void setSurfaceParticleType(SurfaceParticleType v) {
        this.surfaceParticleType = v;
        // Particles require physics to be meaningful; auto-enable when activating
        if (v != SurfaceParticleType.NONE) this.physicsEnabled = true;
        markChangedAndSync();
    }

    /** Reset all display/physics fields to their defaults. */
    public void resetToDefaults() {
        this.gridSize      = 12;
        this.waveAmp       = 1.0f;
        this.waveSpeed     = 1.0f;
        this.bobAmp        = 0.08f;
        this.bobSpeed      = 1.0f;
        this.surfaceColor  = 0xFFFFFFFF;
        this.spinItem           = false;
        this.voxelMode          = false;
        this.physicsEnabled     = false;
        this.surfaceParticleType = SurfaceParticleType.NONE;
        markChangedAndSync();
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // ── NBT ────────────────────────────────────────────────────────────────

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedItem = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        String uuidStr = input.getStringOr("DepositerUUID", "");
        this.depositerUUID = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
        this.isPrivate = input.getBooleanOr("IsPrivate", false);
        this.gridSize = Math.max(1, Math.min(16, input.getIntOr("GridSize", 8)));
        this.waveAmp = input.getFloatOr("WaveAmp", 1.0f);
        this.waveSpeed = input.getFloatOr("WaveSpeed", 1.0f);
        this.bobAmp = input.getFloatOr("BobAmp", 0.08f);
        this.bobSpeed = input.getFloatOr("BobSpeed", 1.0f);
        this.surfaceColor = input.getIntOr("SurfaceColor", 0xFFFFFFFF);
        this.spinItem = input.getBooleanOr("SpinItem", false);
        this.voxelMode = input.getBooleanOr("VoxelMode", false);
        this.physicsEnabled      = input.getBooleanOr("PhysicsEnabled", false);
        this.surfaceParticleType = SurfaceParticleType.fromString(
                input.getStringOr("SurfaceParticleType", "none"));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!storedItem.isEmpty()) {
            output.store("Item", ItemStack.CODEC, storedItem);
        }
        output.putString("DepositerUUID", depositerUUID != null ? depositerUUID.toString() : "");
        output.putBoolean("IsPrivate", isPrivate);
        output.putInt("GridSize", gridSize);
        output.putFloat("WaveAmp", waveAmp);
        output.putFloat("WaveSpeed", waveSpeed);
        output.putFloat("BobAmp", bobAmp);
        output.putFloat("BobSpeed", bobSpeed);
        output.putInt("SurfaceColor", surfaceColor);
        output.putBoolean("SpinItem", spinItem);
        output.putBoolean("VoxelMode", voxelMode);
        output.putBoolean("PhysicsEnabled", physicsEnabled);
        output.putString("SurfaceParticleType", surfaceParticleType.toSerializedName());
    }

    // ── Client sync ────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
