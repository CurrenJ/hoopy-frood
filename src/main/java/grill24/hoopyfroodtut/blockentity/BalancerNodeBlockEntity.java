package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Block entity for the Balancer Node.
 * <p>
 * Has no inventory of its own.  Each server tick interval it:
 * <ol>
 *   <li>Locates the source block (attached in the FACING.opposite direction) and reads its item
 *       handlers, checking every face.</li>
 *   <li>Walks straight out in the FACING direction to collect every block entity that exposes an
 *       item handler, stopping at the first gap.</li>
 *   <li>Distributes items from the source equally among the destinations using round-robin
 *       ordering so that slow sources (e.g. hopper-fed) don't starve later destinations.</li>
 *   <li>Sets POWERED=true when at least one item was moved, false otherwise.</li>
 * </ol>
 * <p>
 * Balancer Range Extender items can be inserted by right-clicking; each one increases the
 * effective scan range by {@value #RANGE_PER_EXTENDER} blocks. They are dropped when the
 * block is broken.
 */
public class BalancerNodeBlockEntity extends BlockEntity {

    /** Base maximum number of destination blocks scanned in the facing direction. */
    private static final int MAX_RANGE = 4;

    /** Additional scan range granted by each inserted Balancer Range Extender. */
    public static final int RANGE_PER_EXTENDER = 1;

    /**
     * Face priority order for acquiring item handlers from a destination block.
     * Tries cardinal sides first, then top, then bottom, then the direction-agnostic
     * (null) capability last.  Multiple faces may return the same handler instance;
     * callers should deduplicate by identity before storing.
     */
    private static final Direction[] INSERTION_FACE_PRIORITY = {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
        Direction.UP,
        Direction.DOWN
    };

    /**
     * Index into the destination list at which the next distribution tick will start.
     * Persisted so that a server reload doesn't reset the round-robin position.
     */
    private int nextDestIndex = 0;

    /** Number of Balancer Range Extender items currently stored in this node. */
    private int storedExtenders = 0;

    public BalancerNodeBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.BALANCER_NODE.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Range extender management
    // -------------------------------------------------------------------------

    /** Effective scan range, accounting for inserted range extenders. */
    public int effectiveRange() {
        return MAX_RANGE + storedExtenders * RANGE_PER_EXTENDER;
    }

    /** Inserts one Balancer Range Extender into this node. */
    public void addExtender() {
        storedExtenders++;
        setChanged();
    }

    /** Returns how many range extenders are stored in this node. */
    public int getStoredExtenders() {
        return storedExtenders;
    }

    /**
     * Drops all stored Balancer Range Extender items as item entities at {@code pos}.
     * Called when the block is broken.
     */
    public void dropExtenders(Level level, BlockPos pos) {
        for (int i = 0; i < storedExtenders; i++) {
            Block.popResource(level, pos, new ItemStack(HoopyFroodItems.BALANCER_RANGE_EXTENDER.get()));
        }
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.nextDestIndex    = input.getIntOr("NextDestIndex",   0);
        this.storedExtenders  = input.getIntOr("StoredExtenders", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("NextDestIndex",   nextDestIndex);
        output.putInt("StoredExtenders", storedExtenders);
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, BalancerNodeBlockEntity entity) {
        if (level.getGameTime() % Config.BALANCER_TICK_RATE.get() != 0) return;

        Direction facing = state.getValue(BalancerNode.FACING);

        // --- Source ---
        BlockPos sourcePos = pos.relative(facing.getOpposite());
        ResourceHandler<ItemResource> source = getHandlerFromAnySide(level, sourcePos);
        if (source == null) {
            setPowered(level, pos, state, false);
            return;
        }

        // --- Destinations ---
        List<List<ResourceHandler<ItemResource>>> destinations = new ArrayList<>();
        BlockPos scanPos = pos.relative(facing);
        for (int i = 0; i < entity.effectiveRange(); i++) {
            List<ResourceHandler<ItemResource>> handlers = getHandlersForDestination(level, scanPos);
            if (handlers.isEmpty()) break;
            destinations.add(handlers);
            scanPos = scanPos.relative(facing);
        }

        if (destinations.isEmpty()) {
            setPowered(level, pos, state, false);
            return;
        }

        boolean transferred = entity.distributeItems(source, destinations);
        setPowered(level, pos, state, transferred);
    }

    // -------------------------------------------------------------------------
    // Particle preview
    // -------------------------------------------------------------------------

    /**
     * Spawns a visual preview of the source and all potential destinations within
     * the effective range.  Called server-side on shift right-click with empty hand.
     * <ul>
     *   <li>{@link ParticleTypes#END_ROD}      — source block with a recognised item handler</li>
     *   <li>{@link ParticleTypes#HAPPY_VILLAGER} — destination block with a recognised item handler</li>
     *   <li>{@link ParticleTypes#SMOKE}         — non-air block within range that has no item handler</li>
     * </ul>
     */
    public void spawnPreviewParticles(ServerLevel level, BlockPos nodePos, BlockState state) {
        Direction facing = state.getValue(BalancerNode.FACING);

        // Source
        BlockPos sourcePos = nodePos.relative(facing.getOpposite());
        if (getHandlerFromAnySide(level, sourcePos) != null) {
            spawnCluster(level, sourcePos, ParticleTypes.END_ROD, 10);
        }

        // Scan the full effective range (no early-exit on gaps) so the player can
        // see where active sinks are and where potential sink spots exist.
        BlockPos scanPos = nodePos.relative(facing);
        for (int i = 0; i < effectiveRange(); i++) {
            boolean hasSink = !getHandlersForDestination(level, scanPos).isEmpty();
            spawnCluster(level, scanPos,
                    hasSink ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE,
                    hasSink ? 8 : 4);
            scanPos = scanPos.relative(facing);
        }
    }

    private static void spawnCluster(ServerLevel level, BlockPos pos, ParticleOptions particle, int count) {
        level.sendParticles(particle,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                count, 0.2, 0.1, 0.2, 0.05);
    }

    // -------------------------------------------------------------------------
    // Item handler helpers
    // -------------------------------------------------------------------------

    @Nullable
    static ResourceHandler<ItemResource> getHandlerFromAnySide(Level level, BlockPos pos) {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (handler != null) return handler;
        for (Direction dir : Direction.values()) {
            handler = level.getCapability(Capabilities.Item.BLOCK, pos, dir);
            if (handler != null) return handler;
        }
        return null;
    }

    /**
     * Returns all distinct item handlers exposed by the block at {@code pos},
     * in insertion-priority order.  Deduplicated by reference identity.
     */
    static List<ResourceHandler<ItemResource>> getHandlersForDestination(Level level, BlockPos pos) {
        Map<ResourceHandler<ItemResource>, Boolean> seen = new IdentityHashMap<>();
        List<ResourceHandler<ItemResource>> result = new ArrayList<>();

        for (Direction face : INSERTION_FACE_PRIORITY) {
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, face);
            if (handler != null && seen.put(handler, Boolean.TRUE) == null) {
                result.add(handler);
            }
        }
        ResourceHandler<ItemResource> universal = level.getCapability(Capabilities.Item.BLOCK, pos, null);
        if (universal != null && seen.put(universal, Boolean.TRUE) == null) {
            result.add(universal);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Distribution algorithm
    // -------------------------------------------------------------------------

    private boolean distributeItems(ResourceHandler<ItemResource> source, List<List<ResourceHandler<ItemResource>>> destinations) {
        boolean transferred = false;
        int numDests = destinations.size();

        for (int srcIndex = 0; srcIndex < source.size(); srcIndex++) {
            ItemResource resource = source.getResource(srcIndex);
            if (resource.isEmpty()) continue;

            int available = source.getAmountAsInt(srcIndex);
            int perDest = Math.min(Config.BALANCER_BATCH_SIZE.get(), Math.max(1, available / numDests));

            for (int i = 0; i < numDests; i++) {
                if (available <= 0) break;

                int destIndex = (nextDestIndex + i) % numDests;
                int toMove = Math.min(perDest, available);

                for (ResourceHandler<ItemResource> dest : destinations.get(destIndex)) {
                    int canInsert;
                    try (var tx = Transaction.openRoot()) {
                        canInsert = dest.insert(resource, toMove, tx);
                    }
                    if (canInsert <= 0) continue;

                    try (var tx = Transaction.openRoot()) {
                        int extracted = source.extract(srcIndex, resource, canInsert, tx);
                        if (extracted > 0) {
                            int inserted = dest.insert(resource, extracted, tx);
                            if (inserted > 0) {
                                tx.commit();
                                available -= inserted;
                                nextDestIndex = (destIndex + 1) % numDests;
                                setChanged();
                                transferred = true;
                            }
                        }
                    }
                    break;
                }
            }
        }

        return transferred;
    }

    // -------------------------------------------------------------------------
    // State helpers
    // -------------------------------------------------------------------------

    private static void setPowered(Level level, BlockPos pos, BlockState state, boolean powered) {
        if (state.getValue(BalancerNode.POWERED) != powered) {
            level.setBlock(pos, state.setValue(BalancerNode.POWERED, powered), Block.UPDATE_ALL);
        }
    }
}
