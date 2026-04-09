package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

/**
 * Block entity for the Banishing Bin.
 *
 * <p>Items are accepted via hopper insertion into the {@link Container} slot.
 *
 * Each item is assigned to one of three rings (round-robin) and scheduled for banishment
 * after {@link #ORBIT_TICKS} ticks. Banishment teleports the item to a deterministic,
 * extremely distant coordinate seeded by the item's registry name — so the same item
 * type always ends up at the same forsaken coordinate.
 */
public class BanishingBinBlockEntity extends BlockEntity implements Container {

    // ── Timing constants ──────────────────────────────────────────────────────

    /** Ticks an item orbits before being banished (0.75 s at 20 TPS). */
    public static final int ORBIT_TICKS = 15;

    /** Ticks after the last item received before the bin returns to inactive. (2 s) */
    public static final int ACTIVITY_WINDOW = 200;

    /** Maximum number of unique item types the bin will remember. */
    public static final int MAX_BANISHED_TYPES = 64;

    /** Minimum banishment distance from world origin. */
    private static final int MIN_DIST = 100_000;

    /** Maximum banishment distance from world origin. */
    private static final int MAX_DIST = 1_000_000;

    /** Y coordinate items are spawned at in the banishment dimension. */
    private static final int BANISH_Y = 64;

    // ── Queued item record ────────────────────────────────────────────────────

    /**
     * An item waiting to be banished.
     *
     * @param stack    The item stack to banish.
     * @param ring     Which ring this item is visually assigned to (0, 1, or 2).
     * @param banishAt The game tick at which this item should be banished.
     */
    public record QueuedItem(ItemStack stack, int ring, long banishAt) {}

    // ── State ─────────────────────────────────────────────────────────────────

    private final ArrayList<QueuedItem> queue = new ArrayList<>();
    /** Insertion-ordered set of every unique item type (ignoring components) ever banished. Capped at {@link #MAX_BANISHED_TYPES}. */
    private final LinkedHashSet<Item> banishedTypes = new LinkedHashSet<>();
    /** The game tick at which the most recent item was received. Used to time the spin-down. */
    private long lastReceivedTick = -1L;

    // ── Client-side animation state (not saved, not synced) ───────────────────

    /** Accumulated orbit angle for ring A (radians). Persists across frames so phases integrate correctly. */
    public float clientRingPhaseA = 0f;
    /** Accumulated orbit angle for ring B (radians). Staggered initial offset for visual variety. */
    public float clientRingPhaseB = (float) (Math.PI * 0.67);
    /** Accumulated orbit angle for ring C (radians). Staggered initial offset for visual variety. */
    public float clientRingPhaseC = (float) (Math.PI * 1.33);
    /** Accumulated precession angle for ring B's tilt axis (radians). */
    public float clientPrecPhaseB = 0f;
    /** Accumulated precession angle for ring C's tilt axis (radians). Offset so rings don't start in sync. */
    public float clientPrecPhaseC = (float) Math.PI;
    /** animTime value from the previous render frame; -1 means first frame. */
    public float clientLastAnimTime = -1f;
    /**
     * The game tick at which the bin first became active after being idle.
     * Unlike {@link #lastReceivedTick}, this is NOT updated on subsequent items while
     * the bin is already active — so the spin-up animation plays through once and
     * does not restart each time a hopper delivers an item.
     */
    private long activationTick = -1L;
    private int nextRing = 0;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BanishingBinBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.BANISHING_BIN.get(), pos, state);
    }

    // ── Server tick ───────────────────────────────────────────────────────────

    public static void tick(Level level, BlockPos pos, BlockState state, BanishingBinBlockEntity be) {
        if (level.isClientSide()) return;

        long gameTime = level.getGameTime();
        boolean changed = false;

        // Process banishments
        var iter = be.queue.iterator();
        while (iter.hasNext()) {
            QueuedItem item = iter.next();
            if (gameTime >= item.banishAt()) {
                banishItem(level, item.stack(), be);
                iter.remove();
                changed = true;
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    /** Teleport the item stack to a seeded destination far from the world origin. */
    private static void banishItem(Level level, ItemStack stack, BanishingBinBlockEntity be) {
        if (!(level instanceof ServerLevel)) return;

        if (be.banishedTypes.size() < MAX_BANISHED_TYPES) {
            be.banishedTypes.add(stack.getItem());
        }

        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        long seed = (long) key.toString().hashCode();
        Random rng = new Random(seed);

        int x = signedRange(rng);
        int z = signedRange(rng);

        ItemEntity entity = new ItemEntity(level, x + 0.5, BANISH_Y, z + 0.5, stack.copy());
        level.addFreshEntity(entity);
    }

    /** Returns a value in the range [±MIN_DIST, ±MAX_DIST]. */
    private static int signedRange(Random rng) {
        int mag = MIN_DIST + rng.nextInt(MAX_DIST - MIN_DIST);
        return rng.nextBoolean() ? mag : -mag;
    }

    private void enqueueItem(ItemStack stack, long gameTime) {
        // Only mark a new activation when the bin was previously idle so that
        // continuous hopper feeding doesn't restart the spin-up animation.
        boolean wasIdle = lastReceivedTick < 0 || (gameTime - lastReceivedTick) > ACTIVITY_WINDOW;
        if (wasIdle) {
            activationTick = gameTime;
        }

        int ring = nextRing % 3;
        nextRing = (nextRing + 1) % Integer.MAX_VALUE;
        queue.add(new QueuedItem(stack.copy(), ring, gameTime + ORBIT_TICKS));
        lastReceivedTick = gameTime;
    }

    // ── Container (hopper input) ──────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return 1;
    }

    /** Always reports empty so hoppers continue offering items. */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /** The virtual input slot is always empty — items are immediately queued. */
    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    /**
     * Called by the hopper to insert an item. The item is immediately queued for
     * banishment rather than stored in a real slot.
     */
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && level != null && !level.isClientSide()) {
            enqueueItem(stack, level.getGameTime());
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {}

    // ── Public accessors ──────────────────────────────────────────────────────

    /** Current list of items queued for banishment. Read by the block's drop logic. */
    public List<QueuedItem> getQueue() {
        return queue;
    }

    /** Insertion-ordered set of every unique item type ever banished by this bin. */
    public LinkedHashSet<Item> getBanishedTypes() {
        return banishedTypes;
    }

    /**
     * Whether the bin is in the active visual state (received an item within the last
     * {@link #ACTIVITY_WINDOW} ticks).
     */
    public boolean isActive() {
        return level != null && lastReceivedTick >= 0
                && level.getGameTime() - lastReceivedTick <= ACTIVITY_WINDOW;
    }

    /** Game tick at which the most recent item was received. -1 if never. */
    public long getLastReceivedTick() {
        return lastReceivedTick;
    }

    /**
     * Game tick at which the bin first became active after being idle.
     * Drives the spin-up animation. Not reset by subsequent items while active.
     */
    public long getActivationTick() {
        return activationTick;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.lastReceivedTick = input.getLongOr("LastReceived", -1L);
        this.activationTick   = input.getLongOr("ActivationTick", -1L);
        this.nextRing = input.getIntOr("NextRing", 0);
        int size = input.getIntOr("QueueSize", 0);
        queue.clear();
        for (int i = 0; i < size; i++) {
            ItemStack stack = input.read("Item" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                int ring = input.getIntOr("Ring" + i, 0);
                long banishAt = input.getLongOr("BanishAt" + i, 0L);
                queue.add(new QueuedItem(stack, ring, banishAt));
            }
        }
        banishedTypes.clear();
        int memSize = input.getIntOr("BanishedTypesSize", 0);
        for (int i = 0; i < memSize; i++) {
            String key = input.getStringOr("BanishedType" + i, "");
            if (!key.isEmpty()) {
                BuiltInRegistries.ITEM.getOptional(Identifier.parse(key))
                        .ifPresent(banishedTypes::add);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("LastReceived", lastReceivedTick);
        output.putLong("ActivationTick", activationTick);
        output.putInt("NextRing", nextRing);
        output.putInt("QueueSize", queue.size());
        for (int i = 0; i < queue.size(); i++) {
            QueuedItem item = queue.get(i);
            output.store("Item" + i, ItemStack.CODEC, item.stack());
            output.putInt("Ring" + i, item.ring());
            output.putLong("BanishAt" + i, item.banishAt());
        }
        List<Item> memList = new ArrayList<>(banishedTypes);
        output.putInt("BanishedTypesSize", memList.size());
        for (int i = 0; i < memList.size(); i++) {
            output.putString("BanishedType" + i, BuiltInRegistries.ITEM.getKey(memList.get(i)).toString());
        }
    }

    // ── Client sync ───────────────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
