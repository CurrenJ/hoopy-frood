package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.block.PrecisionDispenser;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Eliminates projectile spread when dispensing from a {@link PrecisionDispenser}.
 * <p>
 * Vanilla {@code ProjectileDispenseBehavior.execute} passes a non-zero {@code uncertainty}
 * to {@code Projectile.spawnProjectileUsingShoot}, introducing random spread. When the
 * {@link PrecisionDispenser#PRECISION_MODE} flag is active we override that to {@code 0.0F}
 * so every shot follows an identical trajectory.
 */
@Mixin(ProjectileDispenseBehavior.class)
public abstract class ProjectileDispenseBehaviorMixin {

    @Redirect(
        method = "execute",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileUsingShoot(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;DDDFF)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private Projectile preciseProjectileShoot(
            Projectile projectile, ServerLevel level, ItemStack stack,
            double dx, double dy, double dz, float power, float uncertainty) {
        float effective = PrecisionDispenser.PRECISION_MODE.get() ? 0.0F : uncertainty;
        return Projectile.spawnProjectileUsingShoot(projectile, level, stack, dx, dy, dz, power, effective);
    }
}
