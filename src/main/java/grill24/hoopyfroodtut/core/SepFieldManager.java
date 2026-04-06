package grill24.hoopyfroodtut.core;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server-side registry of active Somebody Else's Problem Field blocks.
 * <p>
 * Block entities register themselves here on load and unregister on removal.
 * The {@link #isInField} query is used by the LivingChangeTargetEvent handler
 * and the PathNavigation mixin to suppress mob AI within field radius.
 */
public class SepFieldManager {

    /** Radius (in blocks) of each SEP field's null zone. */
    public static final int FIELD_RADIUS = 8;
    private static final double FIELD_RADIUS_SQ = (double) FIELD_RADIUS * FIELD_RADIUS;

    // All access happens on the server tick thread; plain HashMap is sufficient.
    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_FIELDS = new HashMap<>();

    public static void register(Level level, BlockPos pos) {
        ACTIVE_FIELDS
                .computeIfAbsent(level.dimension(), k -> new HashSet<>())
                .add(pos.immutable());
    }

    public static void unregister(Level level, BlockPos pos) {
        Set<BlockPos> fields = ACTIVE_FIELDS.get(level.dimension());
        if (fields != null) {
            fields.remove(pos);
        }
    }

    /** Clears all registered fields for a dimension (called on level unload). */
    public static void clearDimension(ResourceKey<Level> dimension) {
        ACTIVE_FIELDS.remove(dimension);
    }

    /** Returns true if {@code position} is within the field radius of any active SEP field. */
    public static boolean isInField(Level level, Vec3 position) {
        Set<BlockPos> fields = ACTIVE_FIELDS.get(level.dimension());
        if (fields == null || fields.isEmpty()) return false;

        for (BlockPos fp : fields) {
            double dx = position.x - (fp.getX() + 0.5);
            double dy = position.y - (fp.getY() + 0.5);
            double dz = position.z - (fp.getZ() + 0.5);
            if (dx * dx + dy * dy + dz * dz <= FIELD_RADIUS_SQ) {
                return true;
            }
        }
        return false;
    }
}
