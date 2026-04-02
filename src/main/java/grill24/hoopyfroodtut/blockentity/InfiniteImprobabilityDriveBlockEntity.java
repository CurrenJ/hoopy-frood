package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Block entity for the Infinite Improbability Drive.
 * <p>
 * Holds one item and ticks a conversion timer toward {@link #BASE_CONVERSION_TICKS}.
 * When the timer completes, the held item is replaced with a uniformly random item
 * drawn from the full game registry.
 * <p>
 * Speed scaling: every {@link #SCAN_INTERVAL} ticks the entity counts distinct
 * (non-air) block types within {@link #SCAN_RADIUS} blocks. Each additional distinct
 * type shaves {@link #TICKS_PER_DISTINCT_BLOCK} ticks off the conversion time, down
 * to a floor of {@link #MIN_CONVERSION_TICKS}.
 */
public class InfiniteImprobabilityDriveBlockEntity extends BlockEntity {

    // -------------------------------------------------------------------------
    // Configurable constants
    // -------------------------------------------------------------------------

    /** Base conversion time in ticks (200 = 10 seconds at 20 TPS). */
    public static int BASE_CONVERSION_TICKS = 1200;

    /** Minimum conversion time regardless of surrounding variety (ticks). */
    public static int MIN_CONVERSION_TICKS = 20;

    /**
     * Ticks shaved off the conversion time per distinct block type found in the
     * surrounding scan area. With the defaults, 36 distinct types hit the minimum.
     */
    public static int TICKS_PER_DISTINCT_BLOCK = 20;

    /** Half-size (in blocks) of the cubic scan region around the drive. */
    public static int SCAN_RADIUS = 4;

    /** How often (in ticks) the distinct-block scan is refreshed. */
    private static final int SCAN_INTERVAL = 40;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private ItemStack heldItem = ItemStack.EMPTY;
    private ItemStack cachedItem = ItemStack.EMPTY; // For scale-out animation
    private int conversionProgress = 0;

    // Cached neighbourhood diversity; refreshed every SCAN_INTERVAL ticks.
    private int distinctBlocksCache = 0;
    private int scanCooldown = 0;

    private float lastToggleTime = -1f;

    public InfiniteImprobabilityDriveBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.INFINITE_IMPROBABILITY_DRIVE.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public ItemStack getCachedItem() {
        return cachedItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        // Update cached item for scale-out animation
        if (!stack.isEmpty()) {
            this.cachedItem = stack.copy();
        }
        // If setting to empty, keep cachedItem for scale-out animation
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }

        lastToggleTime = level != null ? level.getGameTime() : -1f;
    }

    public void resetProgress() {
        this.conversionProgress = 0;
        setChanged();
    }

    public int getConversionProgress() {
        return conversionProgress;
    }

    public int getEffectiveConversionTime() {
        return Math.max(MIN_CONVERSION_TICKS,
                BASE_CONVERSION_TICKS - distinctBlocksCache * TICKS_PER_DISTINCT_BLOCK);
    }

    public int getDistinctBlocksCache() {
        return distinctBlocksCache;
    }

    public List<Component> getInfoComponent() {
        // Print the current conversion progress and effective conversion time in seconds.
        float progressSeconds = conversionProgress / 20f;
        float totalSeconds = getEffectiveConversionTime() / 20f;
        MutableComponent progress = Component.literal(String.format("Progress: %.1f/%.1f seconds", progressSeconds, totalSeconds));
        MutableComponent distinct = Component.literal(String.format("Distinct blocks nearby: %d", distinctBlocksCache));

        return List.of(progress, distinct);
    }

    public float getLastToggleTime() {
        return lastToggleTime;
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state,
            InfiniteImprobabilityDriveBlockEntity be) {
        // Periodically refresh the surrounding block diversity count.
        if (be.scanCooldown <= 0) {
            be.distinctBlocksCache = countDistinctBlockTypes(level, pos);
            be.scanCooldown = SCAN_INTERVAL;
        } else {
            be.scanCooldown--;
        }

        if (be.heldItem.isEmpty()) return;

        be.conversionProgress++;

        if (be.conversionProgress >= be.getEffectiveConversionTime()) {
            be.heldItem = pickRandomItem(level);
            be.cachedItem = be.heldItem.copy();
            be.conversionProgress = 0;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);

            // Spawn particles and play sound to indicate conversion
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        50, 1.0, 1.0, 1.0, 0.1);
            }
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
        } else if (be.conversionProgress % 20 == 0) {
            // Persist progress periodically so a crash doesn't reset the full timer.
            be.setChanged();
        }
    }

    /**
     * Picks a uniformly random non-air item from the full game registry.
     */
    private static ItemStack pickRandomItem(Level level) {
        List<Item> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) candidates.add(item);
        }
        if (candidates.isEmpty()) return ItemStack.EMPTY;
        Item chosen = candidates.get(level.getRandom().nextInt(candidates.size()));
        return new ItemStack(chosen);
    }

    /**
     * Counts the number of distinct non-air block types in the cubic region
     * of radius {@link #SCAN_RADIUS} centred on {@code center}.
     */
    private static int countDistinctBlockTypes(Level level, BlockPos center) {
        Set<Block> types = new HashSet<>();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockState s = level.getBlockState(center.offset(dx, dy, dz));
                    if (!s.isAir()) types.add(s.getBlock());
                }
            }
        }
        return types.size();
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.conversionProgress = input.getIntOr("ConversionProgress", 0);
        this.distinctBlocksCache = input.getIntOr("DistinctBlocksCache", 0);
        this.lastToggleTime = input.getFloatOr("LastToggleTime", -1f);

        // Load held item via a 1-slot ItemContainerContents wrapper
        var tmp = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        input.read("HeldItem", ItemContainerContents.CODEC).ifPresent(c -> c.copyInto(tmp));
        this.heldItem = tmp.get(0);

        // Load cached item for scale-out animation
        var cachedTmp = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        input.read("CachedItem", ItemContainerContents.CODEC).ifPresent(c -> c.copyInto(cachedTmp));
        this.cachedItem = cachedTmp.get(0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ConversionProgress", conversionProgress);
        output.putInt("DistinctBlocksCache", distinctBlocksCache);
        output.putFloat("LastToggleTime", lastToggleTime);

        var tmp = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        tmp.set(0, heldItem);
        output.store("HeldItem", ItemContainerContents.CODEC, ItemContainerContents.fromItems(tmp));

        var cachedTmp = net.minecraft.core.NonNullList.withSize(1, ItemStack.EMPTY);
        cachedTmp.set(0, cachedItem);
        output.store("CachedItem", ItemContainerContents.CODEC, ItemContainerContents.fromItems(cachedTmp));
    }

    // -------------------------------------------------------------------------
    // Networking — full state sync so clients can render the held item
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
