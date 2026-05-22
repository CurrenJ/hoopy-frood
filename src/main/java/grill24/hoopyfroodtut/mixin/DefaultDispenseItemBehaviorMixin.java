package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.block.PrecisionDispenser;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Eliminates random velocity scatter for plain item-entity drops from a
 * {@link PrecisionDispenser}.
 * <p>
 * Vanilla {@code DefaultDispenseItemBehavior.execute} calls the static helper
 * {@code spawnItem}, which applies {@link net.minecraft.util.RandomSource#triangle}
 * noise to the initial velocity. When {@link PrecisionDispenser#PRECISION_MODE} is
 * active we bypass that helper and spawn the item with a fixed, deterministic velocity.
 */
@Mixin(DefaultDispenseItemBehavior.class)
public abstract class DefaultDispenseItemBehaviorMixin {

    @Redirect(
        method = "execute",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/core/dispenser/DefaultDispenseItemBehavior;spawnItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;Lnet/minecraft/core/Position;)V")
    )
    private void preciseItemSpawn(Level level, ItemStack stack, int accuracy, Direction direction, Position position) {
        if (!PrecisionDispenser.PRECISION_MODE.get()) {
            DefaultDispenseItemBehavior.spawnItem(level, stack, accuracy, direction, position);
            return;
        }

        double spawnX = position.x();
        double spawnY = position.y();
        double spawnZ = position.z();
        if (direction.getAxis() == Direction.Axis.Y) {
            spawnY -= 0.125;
        } else {
            spawnY -= 0.15625;
        }

        ItemEntity entity = new ItemEntity(level, spawnX, spawnY, spawnZ, stack);
        // Fixed velocity: center of vanilla's random distribution (pow = 0.25, no triangle spread).
        entity.setDeltaMovement(
                direction.getStepX() * 0.25,
                0.2,
                direction.getStepZ() * 0.25
        );
        level.addFreshEntity(entity);
    }
}
