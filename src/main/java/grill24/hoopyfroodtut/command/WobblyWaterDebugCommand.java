package grill24.hoopyfroodtut.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import grill24.hoopyfroodtut.block.WobblyWater;
import grill24.hoopyfroodtut.blockentity.WobblyWaterBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Function;

/**
 * Debug command for tweaking all Wobbly Water display and physics properties at runtime.
 *
 * <pre>
 *   /ww get
 *   /ww grid        &lt;1–32&gt;
 *   /ww wave_amp    &lt;0–10&gt;
 *   /ww wave_speed  &lt;0–10&gt;
 *   /ww bob_amp     &lt;0–2&gt;
 *   /ww bob_speed   &lt;0–10&gt;
 *   /ww color       &lt;AARRGGBB hex, e.g. FFFF3D00&gt;
 *   /ww spin        &lt;true|false&gt;
 *   /ww voxel       &lt;true|false&gt;
 *   /ww physics          &lt;true|false&gt;
 *   /ww particle_size_min &lt;0–2&gt;
 *   /ww particle_size_max &lt;0–2&gt;
 *   /ww texture          &lt;water|lava|slime|honey|magma&gt;
 *   /ww reset
 * </pre>
 *
 * All subcommands target the Wobbly Water block the executing player is looking at (up to 5 blocks away).
 * Requires permission level 2 (op).
 */
public class WobblyWaterDebugCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ww")
            .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_ADMIN))

            // ── get ──────────────────────────────────────────────────────────
            .then(Commands.literal("get")
                .executes(ctx -> get(ctx.getSource())))

            // ── grid ─────────────────────────────────────────────────────────
            .then(Commands.literal("grid")
                .then(Commands.argument("value", IntegerArgumentType.integer(1, 32))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        int v = IntegerArgumentType.getInteger(ctx, "value");
                        be.setGridSize(v);
                        return "gridSize → " + v;
                    }))))

            // ── wave_amp ─────────────────────────────────────────────────────
            .then(Commands.literal("wave_amp")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 10f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setWaveAmp(v);
                        return "waveAmp → " + v;
                    }))))

            // ── wave_speed ───────────────────────────────────────────────────
            .then(Commands.literal("wave_speed")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 10f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setWaveSpeed(v);
                        return "waveSpeed → " + v;
                    }))))

            // ── bob_amp ──────────────────────────────────────────────────────
            .then(Commands.literal("bob_amp")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 2f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setBobAmp(v);
                        return "bobAmp → " + v;
                    }))))

            // ── bob_speed ────────────────────────────────────────────────────
            .then(Commands.literal("bob_speed")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 10f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setBobSpeed(v);
                        return "bobSpeed → " + v;
                    }))))

            // ── color (AARRGGBB hex string) ──────────────────────────────────
            .then(Commands.literal("color")
                .then(Commands.argument("argb_hex", StringArgumentType.word())
                    .executes(ctx -> {
                        String hex = StringArgumentType.getString(ctx, "argb_hex").replaceFirst("^#", "");
                        try {
                            int color = (int) Long.parseLong(hex, 16);
                            return modifyBE(ctx.getSource(), be -> {
                                be.setSurfaceColor(color);
                                return "surfaceColor → #" + String.format("%08X", color);
                            });
                        } catch (NumberFormatException e) {
                            ctx.getSource().sendFailure(Component.literal(
                                "Invalid hex color '" + hex + "'. Use AARRGGBB format, e.g. FFFF3D00"));
                            return 0;
                        }
                    })))

            // ── spin ─────────────────────────────────────────────────────────
            .then(Commands.literal("spin")
                .then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        boolean v = BoolArgumentType.getBool(ctx, "value");
                        be.setSpinItem(v);
                        return "spinItem → " + v;
                    }))))

            // ── voxel ────────────────────────────────────────────────────────
            .then(Commands.literal("voxel")
                .then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        boolean v = BoolArgumentType.getBool(ctx, "value");
                        be.setVoxelMode(v);
                        return "voxelMode → " + v;
                    }))))

            // ── physics ──────────────────────────────────────────────────────
            .then(Commands.literal("physics")
                .then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        boolean v = BoolArgumentType.getBool(ctx, "value");
                        be.setPhysicsEnabled(v);
                        return "physicsEnabled → " + v;
                    }))))

            // ── particle_size_min ────────────────────────────────────────────
            .then(Commands.literal("particle_size_min")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 2f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setParticleSizeMin(v);
                        return "particleSizeMin → " + v;
                    }))))

            // ── particle_size_max ────────────────────────────────────────────
            .then(Commands.literal("particle_size_max")
                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 2f))
                    .executes(ctx -> modifyBE(ctx.getSource(), be -> {
                        float v = FloatArgumentType.getFloat(ctx, "value");
                        be.setParticleSizeMax(v);
                        return "particleSizeMax → " + v;
                    }))))

            // ── texture (block-state property) ───────────────────────────────
            .then(Commands.literal("texture")
                .then(Commands.argument("name", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (WobblyWater.SurfaceTexture t
                                : WobblyWater.SurfaceTexture.values()) {
                            builder.suggest(t.getSerializedName());
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> setTexture(ctx.getSource(),
                            StringArgumentType.getString(ctx, "name")))))

            // ── reset ────────────────────────────────────────────────────────
            .then(Commands.literal("reset")
                .executes(ctx -> reset(ctx.getSource())))
        );
    }

    // ── Command handlers ───────────────────────────────────────────────────

    private static int get(CommandSourceStack source) {
        return withTarget(source, (pos, be, level) -> {
            source.sendSuccess(() -> Component.literal(
                    "[WW @ " + pos.toShortString() + "]\n" +
                    "  gridSize="      + be.getGridSize()    + "\n" +
                    "  waveAmp="       + be.getWaveAmp()     + "\n" +
                    "  waveSpeed="     + be.getWaveSpeed()   + "\n" +
                    "  bobAmp="        + be.getBobAmp()      + "\n" +
                    "  bobSpeed="      + be.getBobSpeed()    + "\n" +
                    "  surfaceColor=#" + String.format("%08X", be.getSurfaceColor()) + "\n" +
                    "  spinItem="      + be.isSpinItem()     + "\n" +
                    "  voxelMode="     + be.isVoxelMode()    + "\n" +
                    "  physicsEnabled="+ be.isPhysicsEnabled() + "\n" +
                    "  particleSizeMin="+ be.getParticleSizeMin() + "\n" +
                    "  particleSizeMax="+ be.getParticleSizeMax() + "\n" +
                    "  surfaceTexture="+ level.getBlockState(pos)
                            .getValue(WobblyWater.SURFACE_TEXTURE).getSerializedName()
            ), false);
            return 1;
        });
    }

    private static int setTexture(CommandSourceStack source, String name) {
        WobblyWater.SurfaceTexture texture = null;
        for (WobblyWater.SurfaceTexture t
                : WobblyWater.SurfaceTexture.values()) {
            if (t.getSerializedName().equalsIgnoreCase(name)) {
                texture = t;
                break;
            }
        }
        if (texture == null) {
            source.sendFailure(Component.literal(
                "Unknown texture '" + name + "'. Valid: water, lava, slime, honey, magma"));
            return 0;
        }
        final WobblyWater.SurfaceTexture finalTexture = texture;
        return withTarget(source, (pos, be, level) -> {
            BlockState state = level.getBlockState(pos);
            level.setBlock(pos, state.setValue(WobblyWater.SURFACE_TEXTURE, finalTexture),
                    Block.UPDATE_ALL);
            source.sendSuccess(() -> Component.literal("[WW] surfaceTexture → " + finalTexture.getSerializedName()), false);
            return 1;
        });
    }

    private static int reset(CommandSourceStack source) {
        return withTarget(source, (pos, be, level) -> {
            be.resetToDefaults();
            // Also reset block state texture
            BlockState state = level.getBlockState(pos);
            level.setBlock(pos, state.setValue(WobblyWater.SURFACE_TEXTURE,
                    WobblyWater.SurfaceTexture.WATER), Block.UPDATE_ALL);
            source.sendSuccess(() -> Component.literal("[WW] Reset to defaults."), false);
            return 1;
        });
    }

    // ── Utility helpers ────────────────────────────────────────────────────

    /**
     * Finds the Wobbly Water block the player is looking at, applies {@code action} to its block entity,
     * and returns the command result code.
     */
    private static int modifyBE(CommandSourceStack source,
                                 Function<WobblyWaterBlockEntity, String> action) {
        return withTarget(source, (pos, be, level) -> {
            String msg = action.apply(be);
            source.sendSuccess(() -> Component.literal("[WW] " + msg), false);
            return 1;
        });
    }

    @FunctionalInterface
    private interface TargetAction {
        int run(BlockPos pos, WobblyWaterBlockEntity be, ServerLevel level);
    }

    /**
     * Resolves the Wobbly Water block the executing player is looking at (up to 5 blocks away)
     * and calls {@code action}. Returns 0 on failure with an appropriate chat message.
     */
    private static int withTarget(CommandSourceStack source, TargetAction action) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            HitResult hit = player.pick(5.0, 0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                source.sendFailure(Component.literal("Look at a Wobbly Water block first (within 5 blocks)."));
                return 0;
            }
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            ServerLevel level = source.getLevel();
            if (!(level.getBlockEntity(pos) instanceof WobblyWaterBlockEntity be)) {
                source.sendFailure(Component.literal("That block is not a Wobbly Water."));
                return 0;
            }
            return action.run(pos, be, level);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
