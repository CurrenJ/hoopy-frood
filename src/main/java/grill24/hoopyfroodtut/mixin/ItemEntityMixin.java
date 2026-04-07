package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.blockentity.PersonalPrivateItemPresenterBlockEntity;
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
 * <p>When {@code this} is an {@link ItemEntity} sitting inside a PPIP fluid block,
 * gravity is cancelled and replaced with a proportional surface-seeking term —
 * exactly how vanilla cancels gravity for items in water.
 */
@Mixin(Entity.class)
public abstract class ItemEntityMixin {

    private static final double PPIP_P        = 0.9;   // proportional gain: 60% error corrected per tick
    private static final double PPIP_MAX_RISE  = 0.01;  // max upward speed (blocks/tick)
    private static final double PPIP_MAX_SINK  = 0.3;  // max downward speed while above target
    private static final double PPIP_DRAG      = 0.96;  // horizontal drag, matches vanilla water
    private static final double FLOAT_OFFSET   = 0.3;   // item rests this far below the visual surface

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void ppip_interceptGravity(CallbackInfo ci) {
        if (!((Object) this instanceof ItemEntity item)) return;

        Level level = item.level();
        BlockPos itemBlock = BlockPos.containing(item.getX(), item.getY(), item.getZ());
        if (!(level.getBlockEntity(itemBlock) instanceof PersonalPrivateItemPresenterBlockEntity)) return;

        // Cancel normal gravity and apply surface-targeting movement instead.
        double floatTargetY = itemBlock.getY()
                + PersonalPrivateItemPresenterBlockEntity.FLUID_SURFACE_Y
                - FLOAT_OFFSET;

        double dy    = floatTargetY - item.getY();
        Vec3   motion = item.getDeltaMovement();

        double newVy = PPIP_P * dy;
        newVy = Math.max(-PPIP_MAX_SINK, Math.min(PPIP_MAX_RISE, newVy));

        item.setDeltaMovement(motion.x() * PPIP_DRAG, newVy, motion.z() * PPIP_DRAG);
        item.resetFallDistance();
        ci.cancel();
    }
}
