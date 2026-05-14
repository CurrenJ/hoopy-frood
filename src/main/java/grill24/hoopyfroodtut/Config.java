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

    // ── Lava Neutralizer ──────────────────────────────────────────────────────
    static { SERVER_BUILDER.push("lavaNeutralizer"); }

    public static final ModConfigSpec.IntValue LAVA_NEUTRALIZER_TICK_RATE = SERVER_BUILDER
            .comment("Game ticks between each lava neutralization attempt. Lower = faster.")
            .defineInRange("tickRate", 100, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue LAVA_NEUTRALIZER_RADIUS = SERVER_BUILDER
            .comment("Maximum distance from the neutralizer to search for lava source blocks.")
            .defineInRange("radius", 8, 1, 64);

    public static final ModConfigSpec.DoubleValue LAVA_NEUTRALIZER_CHARGE_DEPLETE_CHANCE = SERVER_BUILDER
            .comment("Chance (0.0-1.0) that a charge is consumed each time the neutralizer places a block.",
                     "0.0 = charges never deplete, 1.0 = every placement consumes a charge.")
            .defineInRange("chargeDepleteChance", 0.25, 0.0, 1.0);

    public static final ModConfigSpec.ConfigValue<String> LAVA_NEUTRALIZER_RECHARGE_ITEM = SERVER_BUILDER
            .comment("Item ID used to recharge the neutralizer (right-click with this item).")
            .define("rechargeItem", "minecraft:blaze_powder");

    static { SERVER_BUILDER.pop(); }

    public static final ModConfigSpec SPEC = BUILDER.build();
    public static final ModConfigSpec SERVER_SPEC = SERVER_BUILDER.build();
}
