package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.core.SepFieldManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into {@link PathNavigation} to suppress pathfinding requests that would
 * send a mob into a Somebody Else's Problem field zone.
 *
 * <ul>
 *   <li>{@code createPath(Entity, int)} — returns {@code null} (no path found)
 *       when the target entity is inside any active SEP field radius.</li>
 *   <li>{@code moveTo(double, double, double, double)} — returns {@code false}
 *       (movement rejected) when the destination coordinate is inside any active
 *       SEP field radius.</li>
 * </ul>
 *
 * Both checks are server-side only; the client's copy of {@link SepFieldManager}
 * is always empty so the fast-exit is free on the client.
 */
@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {

    @Shadow @Final protected Mob mob;
    @Shadow @Final protected Level level;

    /**
     * Block path creation toward an entity that is currently inside a SEP field.
     * Called by {@code moveTo(Entity, double)} before any path is computed.
     */
    @Inject(
            method = "createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sepField_skipEntityPath(Entity target, int reachRange,
            CallbackInfoReturnable<Path> cir) {
        if (!level.isClientSide() && SepFieldManager.isInField(level, target.position())) {
            cir.setReturnValue(null);
        }
    }

    /**
     * Block movement commands whose destination coordinate falls inside a SEP field.
     * Covers direct {@code moveTo(x, y, z, speed)} calls from goal code.
     */
    @Inject(
            method = "moveTo(DDDD)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sepField_skipMoveToInField(double x, double y, double z, double speedModifier,
            CallbackInfoReturnable<Boolean> cir) {
        if (!level.isClientSide() && SepFieldManager.isInField(level, new Vec3(x, y, z))) {
            cir.setReturnValue(false);
        }
    }
}
