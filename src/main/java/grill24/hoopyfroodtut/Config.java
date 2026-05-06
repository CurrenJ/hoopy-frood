package grill24.hoopyfroodtut;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BALANCER_TICK_RATE = BUILDER
            .comment("How many game ticks between each Balancer Node transfer attempt. Lower = faster.")
            .defineInRange("balancerTickRate", 20, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BALANCER_BATCH_SIZE = BUILDER
            .comment("Maximum items sent to each destination per transfer attempt.")
            .defineInRange("balancerBatchSize", 4, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BALANCER_ACTIVITY_COOLDOWN = BUILDER
            .comment("Ticks of inactivity before the Balancer Node animations spin down. Lower = snappier idle transition.")
            .defineInRange("balancerActivityCooldown", 60, 1, Integer.MAX_VALUE);

    // ── Wobbly Water ──────────────────────────────────────────────────────────
    static { BUILDER.push("wobblyWater"); }

    public static final ModConfigSpec.BooleanValue WOBBLY_WATER_ENTITY_IMPACTS = BUILDER
            .comment("When enabled, nearby items and players that cross a Wobbly Water surface",
                     "generate splash ripples. Scans entities each frame per block.",
                     "Disable on lower-end machines if Wobbly Water causes lag near entities.")
            .define("entityImpacts", true);

    public static final ModConfigSpec.BooleanValue WOBBLY_WATER_PARTICLE_IMPACTS = BUILDER
            .comment("When enabled, in-world particles (rain, splashes, etc.) that fall through",
                     "a Wobbly Water surface generate small displacement ripples.",
                     "Disable on lower-end machines if the extra particle scanning causes lag.")
            .define("particleImpacts", true);

    public static final ModConfigSpec.BooleanValue WOBBLY_WATER_SURFACE_PARTICLES = BUILDER
            .comment("When enabled, Wobbly Water blocks configured with surface particles",
                     "(pink petals, leaf litter) will render them.",
                     "Disable to skip particle rendering on all Wobbly Water blocks.")
            .define("surfaceParticles", true);

    public static final ModConfigSpec.IntValue WOBBLY_WATER_MAX_GRID_SIZE = BUILDER
            .comment("Global cap on the physics/render grid resolution for all Wobbly Water blocks.",
                     "Physics simulation cost is O(n²) — halving this value gives a ~4x speedup.",
                     "Per-block grid sizes above this value are silently clamped down.")
            .defineInRange("maxGridSize", 32, 1, 32);

    static { BUILDER.pop(); }

    // ── Magic Mirror ──────────────────────────────────────────────────────────
    static { BUILDER.push("magicMirror"); }

    public static final ModConfigSpec.IntValue MAGIC_MIRROR_BLOCKS_PER_LEVEL = BUILDER
            .comment("How many blocks of travel cost 1 XP level when using the Magic Mirror.",
                     "Example: 1000 means a 1000-block teleport costs 1 level, 2000 blocks costs 2 levels, etc.",
                     "Cost is always rounded up, and is free when distance is effectively zero.")
            .defineInRange("blocksPerLevel", 1000, 1, Integer.MAX_VALUE);

    static { BUILDER.pop(); }

    // ── Round-Trip Magic Mirror ──────────────────────────────────────────────────
    static { BUILDER.push("roundTripMagicMirror"); }

    public static final ModConfigSpec.IntValue ROUND_TRIP_MAGIC_MIRROR_MAX_DURABILITY = BUILDER
            .comment("Maximum durability (number of teleport uses) for the Round-Trip Magic Mirror.",
                     "A value of 2 gives one full round trip (teleport to spawn, then back to origin).",
                     "Higher values allow multiple round trips before the mirror shatters.")
            .defineInRange("maxDurability", 6, 1, Integer.MAX_VALUE);

    static { BUILDER.pop(); }

    // ── Inert TNT ──────────────────────────────────────────────────────────────
    static { BUILDER.push("inertTnt"); }

    public static final ModConfigSpec.BooleanValue INERT_TNT_KNOCKBACK = BUILDER
            .comment("When enabled, Inert TNT explosions apply knockback to nearby entities.",
                     "Entity damage is always zero regardless of this setting.")
            .define("knockback", false);

    static { BUILDER.pop(); }

    // ── Sturdy Pistons (server config) ────────────────────────────────────────────
    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();

    static { SERVER_BUILDER.push("sturdyPistons"); }

    public static final ModConfigSpec.IntValue STURDY_PISTON_PUSH_LIMIT = SERVER_BUILDER
            .comment("Maximum number of blocks a Sturdy Piston can push or pull in a single operation.",
                     "Vanilla pistons are hardcoded to 12.")
            .defineInRange("pushLimit", 24, 1, 1024);

    static { SERVER_BUILDER.pop(); }

    public static final ModConfigSpec SPEC = BUILDER.build();
    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
}
