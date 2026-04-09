package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.blockentity.WobblyWaterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects at the HEAD of {@link Entity#applyGravity()} (where the method is
 * actually declared) so Mixin can find it without any refMap / invokevirtual
 * owner-class ambiguity.
 *
 * <p>When {@code this} is an {@link ItemEntity} sitting inside a Wobbly Water fluid block,
 * gravity is cancelled and replaced with a proportional surface-seeking term —
 * exactly how vanilla cancels gravity for items in water.
 */
@Mixin(Entity.class)
public abstract class ItemEntityMixin {

    private static final double WW_P        = 0.9;   // proportional gain: 60% error corrected per tick
    private static final double WW_MAX_RISE  = 0.01;  // max upward speed (blocks/tick)
    private static final double WW_MAX_SINK  = 0.3;  // max downward speed while above target
    private static final double WW_DRAG      = 0.96;  // horizontal drag, matches vanilla water
    private static final double FLOAT_OFFSET   = 0.3;   // item rests this far below the visual surface

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void ww_interceptGravity(CallbackInfo ci) {
        if (!((Object) this instanceof ItemEntity item)) return;

        Level level = item.level();
        BlockPos itemBlock = BlockPos.containing(item.getX(), item.getY(), item.getZ());
        if (!(level.getBlockEntity(itemBlock) instanceof WobblyWaterBlockEntity)) return;

        // Cancel normal gravity and apply surface-targeting movement instead.
        double floatTargetY = itemBlock.getY()
                + WobblyWaterBlockEntity.FLUID_SURFACE_Y
                - FLOAT_OFFSET;

        double dy    = floatTargetY - item.getY();
        Vec3   motion = item.getDeltaMovement();

        double newVy = WW_P * dy;
        newVy = Math.max(-WW_MAX_SINK, Math.min(WW_MAX_RISE, newVy));

        item.setDeltaMovement(motion.x() * WW_DRAG, newVy, motion.z() * WW_DRAG);
        item.resetFallDistance();
        ci.cancel();
    }
}
