package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.BeggingItemScrabbler;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block entity for the Begging Item Scrabbler.
 * <p>
 * Every server tick the scrabbler runs one of two state branches:
 * <ul>
 *   <li><b>HUNTING</b> – searches for nearby {@link ItemEntity ItemEntities}, moves toward
 *       the closest one, and collects it into an internal 4-slot inventory.</li>
 *   <li><b>RETURNING</b> – moves back to the configured {@link #homePos} container and
 *       deposits its inventory via the Transfer API.</li>
 * </ul>
 * The scrabbler is in RETURNING state whenever it is carrying items <em>and</em> a home
 * position has been set. If the inventory fills completely without a home set, items are
 * dropped at the scrabbler's feet.
 * <p>
 * Fuel is stored as a nugget count; every block of movement costs one nugget. The home
 * position is set by shift-right-clicking a container block while holding the scrabbler item.
 * The scrabbler emits a comparator signal (0–15) proportional to how full its inventory is.
 */
public class BeggingItemScrabblerBlockEntity extends MovingBlockEntity implements Container {

    /** Horizontal + vertical search radius in blocks. */
    public static final int SEARCH_RADIUS = 12;

    /** Default number of inventory slots when first crafted. */
    public static final int DEFAULT_INVENTORY_SIZE = 4;

    /** Maximum number of inventory slots (each chest upgrade adds 1). */
    public static final int MAX_INVENTORY_SIZE = 27;

    /** Ticks to wait after collecting or depositing before acting again. */
    private static final int COLLECT_COOLDOWN = 20;

    /** Light level (0–15) below which the scrabbler will attempt to place a torch. */
    private static final int TORCH_LIGHT_THRESHOLD = 8;

    /** Ticks to wait between torch placement attempts. */
    private static final int TORCH_PLACE_COOLDOWN = 60;

    /** Maximum A* nodes to expand before falling back to greedy navigation. */
    private static final int ASTAR_MAX_NODES = 1024;

    /** Manhattan distance above which A* is skipped in favour of greedy navigation. */
    private static final int ASTAR_MAX_DISTANCE = 256;

    /**
     * Extra path cost added for a step that lands on an ungrounded position (no solid
     * block directly beneath). Keeps the heuristic admissible since the minimum step
     * cost remains 1.
     */
    private static final int UNGROUNDED_STEP_PENALTY = 4;

    /** Extra path cost added for a step that moves through a fluid (water or lava). */
    private static final int FLUID_STEP_PENALTY = 2;

    private int nuggets = 0;
    private int cooldown = 0;
    private int torchPlaceCooldown = 0;
    @Nullable private UUID targetItemId = null;
    private int inventorySize = DEFAULT_INVENTORY_SIZE;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(DEFAULT_INVENTORY_SIZE, ItemStack.EMPTY);
    @Nullable private BlockPos homePos = null;

    public BeggingItemScrabblerBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.BEGGING_ITEM_SCRABBLER.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getNuggets() { return nuggets; }

    public void addNuggets(int amount) {
        this.nuggets += amount;
        setChanged();
    }

    public @Nullable UUID getTargetItemId() { return targetItemId; }

    public @Nullable BlockPos getHomePos() { return homePos; }

    public void setHomePos(@Nullable BlockPos pos) {
        this.homePos = pos;
        setChanged();
    }

    public int getInventorySize() { return inventorySize; }

    /**
     * Adds one inventory slot, up to {@link #MAX_INVENTORY_SIZE}. Returns {@code true} if the upgrade
     * was applied, {@code false} if already at the maximum.
     */
    public boolean upgradeOneSlot() {
        if (inventorySize >= MAX_INVENTORY_SIZE) return false;
        inventorySize++;
        NonNullList<ItemStack> grown = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); i++) grown.set(i, inventory.get(i));
        inventory = grown;
        setChanged();
        return true;
    }

    /**
     * Sets the inventory capacity directly (used when transferring from item component).
     * Clamps to [{@link #DEFAULT_INVENTORY_SIZE}, {@link #MAX_INVENTORY_SIZE}].
     */
    public void setInventorySize(int size) {
        size = Math.max(DEFAULT_INVENTORY_SIZE, Math.min(MAX_INVENTORY_SIZE, size));
        if (size == inventorySize) return;
        NonNullList<ItemStack> resized = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(size, inventory.size()); i++) resized.set(i, inventory.get(i));
        inventory = resized;
        inventorySize = size;
        setChanged();
    }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    public boolean hasItems() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return true;
        return false;
    }

    public boolean isInventoryFull() {
        for (ItemStack s : inventory) if (s.isEmpty()) return false;
        return true;
    }

    /** True when the scrabbler has collected items and knows where home is. */
    public boolean isReturning() { return hasItems() && homePos != null; }

    /** True when the scrabbler is carrying items but has no home — it will follow the nearest player. */
    public boolean isFollowing() { return homePos == null; }

    /** Comparator output: 0 when empty, 15 when all slots occupied. */
    public int getRedstoneSignal() {
        int filled = 0;
        for (ItemStack s : inventory) if (!s.isEmpty()) filled++;
        return (filled * 15) / inventorySize;
    }

    @Override
    public int getAdvanceForwardDuration() {
        return 8;
    }

    // -------------------------------------------------------------------------
    // Container — exposes inventory to hoppers and item pipes via Transfer API
    // -------------------------------------------------------------------------

    @Override
    public int getContainerSize() { return inventorySize; }

    @Override
    public boolean isEmpty() { return !hasItems(); }

    @Override
    public ItemStack getItem(int slot) { return inventory.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = inventory.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = stack.split(count);
        if (stack.isEmpty()) inventory.set(slot, ItemStack.EMPTY);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        inventory.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.size(); i++) inventory.set(i, ItemStack.EMPTY);
        setChanged();
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); // loads advanceTimestamp, moveDirection
        this.nuggets = input.getIntOr("Nuggets", 0);
        this.cooldown = input.getIntOr("Cooldown", 0);
        this.torchPlaceCooldown = input.getIntOr("TorchPlaceCooldown", 0);
        String uuidStr = input.getStringOr("TargetItemId", "");
        this.targetItemId = uuidStr.isEmpty() ? null : UUID.fromString(uuidStr);
        // Load inventory size before recreating the list so copyInto has the right capacity
        this.inventorySize = Math.max(DEFAULT_INVENTORY_SIZE, Math.min(MAX_INVENTORY_SIZE,
                input.getIntOr("InventorySize", DEFAULT_INVENTORY_SIZE)));
        this.inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
        input.read("Inventory", ItemContainerContents.CODEC)
             .ifPresent(c -> c.copyInto(this.inventory));
        this.homePos = input.read("HomePos", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); // saves advanceTimestamp, moveDirection
        output.putInt("Nuggets", nuggets);
        output.putInt("Cooldown", cooldown);
        output.putInt("TorchPlaceCooldown", torchPlaceCooldown);
        output.putString("TargetItemId", targetItemId != null ? targetItemId.toString() : "");
        output.putInt("InventorySize", inventorySize);
        output.store("Inventory", ItemContainerContents.CODEC,
                ItemContainerContents.fromItems(inventory));
        if (homePos != null) output.store("HomePos", BlockPos.CODEC, homePos);
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        if (level.isClientSide()) return;

        // Idle particles: different type depending on current state
        if (be.nuggets > 0 && be.advanceTimestamp < 0 && be.cooldown <= 0 && level.getGameTime() % 10 == 0) {
            if (be.isReturning()) {
                spawnReturningParticle((ServerLevel) level, pos);
            }
        }

        if (be.torchPlaceCooldown > 0) be.torchPlaceCooldown--;

        if (be.cooldown > 0) {
            be.cooldown--;
            return;
        }

        // If an advance animation is running, wait for it to finish
        if (be.advanceTimestamp >= 0) {
            if (level.getGameTime() - be.advanceTimestamp < be.getAdvanceForwardDuration()) return;
            completeMoveToNextBlock((ServerLevel) level, pos, state, be);
            return;
        }

        // Follow mode: if dark and carrying torches, place one in an adjacent spot
        if (be.isFollowing() && be.torchPlaceCooldown <= 0) {
            if (tryPlaceTorchNearby(level, pos, be)) {
                be.torchPlaceCooldown = TORCH_PLACE_COOLDOWN;
                be.setChanged();
            }
        }

        if (be.nuggets <= 0) return;

        // Return to base only when: inventory is full, OR carrying items with no items left in range.
        // This ensures we fill up as much as possible before making the trip home.
        if (be.isReturning()) {
            if (be.isInventoryFull() || findOrVerifyTarget(level, pos, be) == null) {
                tickReturning(level, pos, state, be);
                return;
            }
            // Items are nearby and inv isn't full — keep hunting
        }

        // Follow mode: no home set but carrying items — move toward nearest player and hold.
        // Switch to following when full OR no items left nearby to collect.
        if (be.isFollowing()) {
            if (be.isInventoryFull() || findOrVerifyTarget(level, pos, be) == null) {
                tickFollowing(level, pos, state, be);
                return;
            }
            // Not full and items nearby — keep hunting
        }

        tickHunting(level, pos, state, be);
    }

    // -------------------------------------------------------------------------
    // RETURNING branch
    // -------------------------------------------------------------------------

    private static void tickReturning(Level level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        BlockPos home = be.homePos; // non-null because isReturning() is true

        // Adjacent to (or at) home → deposit now
        if (pos.closerThan(home, 1.5)) {
            depositAndFinalize(level, pos, state, be);
            return;
        }

        Direction moveDir = aStarNextStep(level, pos, home);
        if (moveDir == null) return; // no path found

        BlockPos nextPos = pos.relative(moveDir);
        if (!isPassable(level, nextPos)) {
            // Next step blocked — wait for it to clear
            return;
        }

        be.nuggets--;
        be.setChanged();
        level.setBlock(pos, state.setValue(BeggingItemScrabbler.FACING, moveDir), Block.UPDATE_CLIENTS);
        be.startAdvance(level, moveDir);
    }

    /**
     * Attempts to deposit all held items into the home container, then drops any
     * remainder and resets to HUNTING mode. If no handler is found at homePos
     * (container was broken), the home position is cleared so the player can re-pair.
     */
    private static void depositAndFinalize(Level level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        boolean foundHandler = tryDeposit(level, be.homePos, be);

        if (!foundHandler) {
            // Container gone — drop everything and forget home so the player can re-pair
            be.dropInventory(level, pos);
            be.homePos = null;
        } else {
            // Container found — pull any available fuel nuggets before leaving
            tryExtractFuel(level, be.homePos, be);
            if (be.hasItems()) {
                // Container full — drop what couldn't fit
                be.dropInventory(level, pos);
            }
        }

        be.cooldown = COLLECT_COOLDOWN;
        be.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    /**
     * Inserts all inventory items into the item handler at {@code home} using the
     * Transfer API. Returns {@code true} if a handler was found (even if it was
     * full), {@code false} if no handler exists at that position.
     */
    private static boolean tryDeposit(Level level, BlockPos home, BeggingItemScrabblerBlockEntity be) {
        ResourceHandler<ItemResource> handler = getHandlerAt(level, home);
        if (handler == null) return false;

        for (int i = 0; i < be.inventory.size(); i++) {
            ItemStack stack = be.inventory.get(i);
            if (stack.isEmpty()) continue;

            ItemResource resource = ItemResource.of(stack);
            int toInsert = stack.getCount();

            try (var tx = Transaction.openRoot()) {
                int inserted = handler.insert(resource, toInsert, tx);
                if (inserted > 0) {
                    tx.commit();
                    stack.shrink(inserted);
                    if (stack.isEmpty()) be.inventory.set(i, ItemStack.EMPTY);
                }
            }
        }
        be.setChanged();
        return true;
    }

    // -------------------------------------------------------------------------
    // FOLLOWING branch — no home set; trail the nearest player and hold items
    // -------------------------------------------------------------------------

    private static void tickFollowing(Level level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SEARCH_RADIUS * 16, false);
        if (nearest == null) return;

        BlockPos playerPos = nearest.blockPosition();

        // Already close enough — just hold items and wait
        if (pos.closerThan(playerPos, 5.0)) return;

        Direction moveDir = aStarNextStep(level, pos, playerPos);
        if (moveDir == null) return;

        BlockPos nextPos = pos.relative(moveDir);
        if (!isPassable(level, nextPos)) return;

        be.nuggets--;
        be.setChanged();
        level.setBlock(pos, state.setValue(BeggingItemScrabbler.FACING, moveDir), Block.UPDATE_CLIENTS);
        be.startAdvance(level, moveDir);
    }

    /**
     * Gives all held inventory items directly to the player (filling their inventory,
     * dropping any overflow at the scrabbler's position). Called on shift+right-click.
     */
    public void extractItemsToPlayer(Player player, Level level, BlockPos pos, BlockState state) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (stack.isEmpty()) continue;
            ItemStack toGive = stack.copy();
            inventory.set(i, ItemStack.EMPTY);
            if (!player.getInventory().add(toGive)) {
                Block.popResource(level, pos, toGive);
            }
        }
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    // -------------------------------------------------------------------------
    // HUNTING branch
    // -------------------------------------------------------------------------

    private static void tickHunting(Level level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        ItemEntity target = findOrVerifyTarget(level, pos, be);
        if (target == null) return;

        BlockPos targetBlockPos = BlockPos.containing(target.position());

        if (pos.equals(targetBlockPos) || pos.closerThan(targetBlockPos, 1.5)) {
            collectItem(level, pos, state, target, be);
            return;
        }

        Direction moveDir = aStarNextStep(level, pos, targetBlockPos);
        if (moveDir == null) {
            be.targetItemId = null;
            be.setChanged();
            return;
        }

        BlockPos nextPos = pos.relative(moveDir);
        if (!isPassable(level, nextPos)) {
            be.targetItemId = null;
            be.setChanged();
            return;
        }

        be.nuggets--;
        be.setChanged();
        level.setBlock(pos, state.setValue(BeggingItemScrabbler.FACING, moveDir), Block.UPDATE_CLIENTS);
        be.startAdvance(level, moveDir);
    }

    /**
     * Discards the target {@link ItemEntity} and places it into the internal inventory.
     * Any overflow that doesn't fit is dropped at the scrabbler's feet.
     */
    private static void collectItem(Level level, BlockPos pos, BlockState state, ItemEntity item, BeggingItemScrabblerBlockEntity be) {
        ItemStack stack = item.getItem().copy();
        item.discard();
        addToInventory(be.inventory, stack, level, pos);
        be.targetItemId = null;
        be.cooldown = COLLECT_COOLDOWN;
        be.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    private static void addToInventory(NonNullList<ItemStack> inventory, ItemStack stack, Level level, BlockPos pos) {
        // Try to merge with an existing stack of the same type
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack slot = inventory.get(i);
            if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, stack)) continue;
            int space = slot.getMaxStackSize() - slot.getCount();
            int toAdd = Math.min(space, stack.getCount());
            slot.grow(toAdd);
            stack.shrink(toAdd);
            if (stack.isEmpty()) return;
        }
        // Place in first empty slot
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).isEmpty()) {
                inventory.set(i, stack.copy());
                return;
            }
        }
        // Inventory full — drop overflow
        Block.popResource(level, pos, stack);
    }

    private void dropInventory(Level level, BlockPos pos) {
        for (int i = 0; i < inventory.size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                Block.popResource(level, pos, inventory.get(i));
                inventory.set(i, ItemStack.EMPTY);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Movement — relocate block + transfer state to new position
    // -------------------------------------------------------------------------

    private static void completeMoveToNextBlock(ServerLevel level, BlockPos pos, BlockState state, BeggingItemScrabblerBlockEntity be) {
        BlockPos nextPos = pos.relative(be.moveDirection);

        if (!isPassable(level, nextPos)) {
            // Something moved in during the animation — refund the nugget and abort
            be.nuggets++;
            be.advanceTimestamp = -1;
            be.setChanged();
            level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), Block.UPDATE_ALL);
            return;
        }

        // Snapshot mutable state before destroying self
        int nuggets = be.nuggets;
        UUID targetItemId = be.targetItemId;
        Direction dir = be.moveDirection;
        BlockPos homePos = be.homePos;
        int torchPlaceCooldown = be.torchPlaceCooldown;
        int invSize = be.inventorySize;
        NonNullList<ItemStack> invSnapshot = NonNullList.withSize(invSize, ItemStack.EMPTY);
        for (int i = 0; i < invSize; i++) invSnapshot.set(i, be.inventory.get(i).copy());

        // Place copy at new position
        BlockState nextBlockState = HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get()
                .defaultBlockState()
                .setValue(BeggingItemScrabbler.FACING, dir);
        level.setBlock(nextPos, nextBlockState, Block.UPDATE_ALL);

        if (level.getBlockEntity(nextPos) instanceof BeggingItemScrabblerBlockEntity newBE) {
            newBE.nuggets = nuggets;
            newBE.targetItemId = targetItemId;
            newBE.moveDirection = dir;
            newBE.advanceTimestamp = -1;
            newBE.homePos = homePos;
            newBE.torchPlaceCooldown = torchPlaceCooldown;
            newBE.inventorySize = invSize;
            NonNullList<ItemStack> newInv = NonNullList.withSize(invSize, ItemStack.EMPTY);
            for (int i = 0; i < invSize; i++) newInv.set(i, invSnapshot.get(i));
            newBE.inventory = newInv;
            newBE.setChanged();
        }

        // Self-destruct with no drops
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a live item handler at {@code pos}, trying the null face first
     * then each cardinal direction.
     */
    @Nullable
    private static ResourceHandler<ItemResource> getHandlerAt(Level level, BlockPos pos) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (h != null) return h;
        for (Direction face : Direction.values()) {
            h = level.getCapability(Capabilities.Item.BLOCK, pos, face);
            if (h != null) return h;
        }
        return null;
    }

    /**
     * Looks up the cached target by UUID, or finds the nearest live {@link ItemEntity}
     * within {@link #SEARCH_RADIUS} blocks.
     */
    @Nullable
    private static ItemEntity findOrVerifyTarget(Level level, BlockPos pos, BeggingItemScrabblerBlockEntity be) {
        AABB searchBox = new AABB(pos).inflate(SEARCH_RADIUS);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox,
                e -> e.isAlive() && !e.getItem().isEmpty());

        if (be.targetItemId != null) {
            for (ItemEntity e : items) {
                if (e.getUUID().equals(be.targetItemId)) return e;
            }
            be.targetItemId = null;
            be.setChanged();
        }

        if (items.isEmpty()) return null;

        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        ItemEntity closest = null;
        double bestDist = Double.MAX_VALUE;
        for (ItemEntity e : items) {
            double d = e.distanceToSqr(cx, cy, cz);
            if (d < bestDist) { bestDist = d; closest = e; }
        }

        if (closest != null) {
            be.targetItemId = closest.getUUID();
            be.setChanged();
        }
        return closest;
    }

    /**
     * Extracts iron and gold nuggets from the item handler at {@code home} and adds
     * them to the scrabbler's fuel reserve.
     */
    private static void tryExtractFuel(Level level, BlockPos home, BeggingItemScrabblerBlockEntity be) {
        ResourceHandler<ItemResource> handler = getHandlerAt(level, home);
        if (handler == null) return;

        ItemResource ironNugget = ItemResource.of(new ItemStack(Items.IRON_NUGGET));
        ItemResource goldNugget = ItemResource.of(new ItemStack(Items.GOLD_NUGGET));

        for (int i = 0; i < handler.size(); i++) {
            ItemResource res = handler.getResource(i);
            if (res.isEmpty()) continue;
            if (!res.equals(ironNugget) && !res.equals(goldNugget)) continue;

            int available = handler.getAmountAsInt(i);
            if (available <= 0) continue;

            try (var tx = Transaction.openRoot()) {
                int extracted = handler.extract(i, res, available, tx);
                if (extracted > 0) {
                    tx.commit();
                    be.nuggets += extracted;
                    be.setChanged();
                }
            }
        }
    }

    /**
     * A* pathfinding: returns the first {@link Direction} step from {@code from} toward
     * {@code to}, routing around non-air blocks. Falls back to {@link #directionToward}
     * when the destination is too far ({@link #ASTAR_MAX_DISTANCE}) or no path is found
     * within {@link #ASTAR_MAX_NODES} expanded nodes.
     */
    @Nullable
    private static Direction aStarNextStep(Level level, BlockPos from, BlockPos to) {
        if (from.equals(to)) return null;
        if (manhattan(from, to) > ASTAR_MAX_DISTANCE) return directionToward(from, to);

        record ANode(int f, int g, BlockPos pos, @Nullable Direction firstDir) {}
        PriorityQueue<ANode> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<BlockPos, Integer> bestG = new HashMap<>();

        bestG.put(from, 0);
        open.add(new ANode(manhattan(from, to), 0, from, null));

        int expanded = 0;
        while (!open.isEmpty() && expanded < ASTAR_MAX_NODES) {
            ANode curr = open.poll();
            if (curr.g > bestG.getOrDefault(curr.pos, Integer.MAX_VALUE)) continue; // stale entry
            if (curr.pos.equals(to)) return curr.firstDir;
            expanded++;

            for (Direction dir : Direction.values()) {
                BlockPos next = curr.pos.relative(dir);
                // Destination itself doesn't need to be air (it's a container or item block)
                boolean passable = next.equals(to) || isPassable(level, next);
                if (!passable) continue;

                // Prefer grounded steps: penalise positions with no solid block below.
                // Skip the penalty at the destination itself so we don't avoid valid targets.
                boolean grounded = next.equals(to)
                        || level.getBlockState(next.below()).isFaceSturdy(level, next.below(), Direction.UP);
                int stepCost = 1 + (grounded ? 0 : UNGROUNDED_STEP_PENALTY);

                int newG = curr.g + stepCost;
                if (newG >= bestG.getOrDefault(next, Integer.MAX_VALUE)) continue;

                bestG.put(next, newG);
                Direction first = curr.firstDir != null ? curr.firstDir : dir;
                open.add(new ANode(newG + manhattan(next, to), newG, next, first));
            }
        }

        return directionToward(from, to); // fallback: no path within budget
    }

    /** A position is passable if it contains only air (fluids break the block on placement). */
    private static boolean isPassable(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    private static int manhattan(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Returns the cardinal direction from {@code from} one step closer to {@code to},
     * preferring the axis with the greatest absolute distance (horizontal axes take
     * priority over vertical when tied).
     */
    private static Direction directionToward(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int absDz = Math.abs(dz);

        if (absDx >= absDz && absDx >= absDy) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (absDz >= absDy) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        } else {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
    }

    /**
     * If the scrabbler is in a dark spot and has torches, places one in the nearest
     * suitable adjacent block (floor below, or wall to any horizontal side).
     * Returns {@code true} if a torch was placed.
     */
    private static boolean tryPlaceTorchNearby(Level level, BlockPos pos, BeggingItemScrabblerBlockEntity be) {
        if (level.getMaxLocalRawBrightness(pos) >= TORCH_LIGHT_THRESHOLD) return false;

        // Find a torch in inventory
        int torchSlot = -1;
        for (int i = 0; i < be.inventory.size(); i++) {
            if (be.inventory.get(i).is(Items.TORCH)) { torchSlot = i; break; }
        }
        if (torchSlot < 0) return false;

        // Try floor placement first (block directly below)
        BlockPos belowPos = pos.below();
        if (level.getBlockState(belowPos).isAir()
                && Block.canSupportCenter(level, belowPos.below(), Direction.UP)) {
            level.setBlock(belowPos, Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
            consumeOneFromSlot(be, torchSlot);
            return true;
        }

        // Try wall placement on each horizontal face
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adjPos = pos.relative(dir);
            if (!level.getBlockState(adjPos).isAir()) continue;

            // The wall is one further step in the same direction; the torch faces back toward the scrabbler
            Direction awayFromWall = dir.getOpposite();
            if (WallTorchBlock.canSurvive(level, adjPos, awayFromWall)) {
                BlockState wallTorch = Blocks.WALL_TORCH.defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, awayFromWall);
                level.setBlock(adjPos, wallTorch, Block.UPDATE_ALL);
                consumeOneFromSlot(be, torchSlot);
                return true;
            }
        }

        return false;
    }

    private static void consumeOneFromSlot(BeggingItemScrabblerBlockEntity be, int slot) {
        ItemStack stack = be.inventory.get(slot);
        stack.shrink(1);
        if (stack.isEmpty()) be.inventory.set(slot, ItemStack.EMPTY);
    }

    private static void spawnReturningParticle(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.HEART, cx, cy + 0.5, cz, 1, 0.2, 0.1, 0.2, 0);
    }
}
