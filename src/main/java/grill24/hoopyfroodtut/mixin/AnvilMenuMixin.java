package grill24.hoopyfroodtut.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows super enchanted books (books with enchantment levels above the natural maximum)
 * to apply their full level when combined with tools in an anvil.
 *
 * <p>Vanilla clamps {@code level} to {@code enchantment.getMaxLevel()} during anvil
 * combination. This redirect lets the cap pass through when the right input is a
 * super enchanted book, so the elevated level survives.</p>
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    /**
     * Returns {@code 255} (the absolute max) when the right-slot item is a super
     * enchanted book, effectively disabling the vanilla level cap for that operation.
     * Otherwise returns the natural max so normal books are unaffected.
     */
    @Redirect(method = "createResultInternal",
              at = @At(value = "INVOKE", ordinal = 0,
                       target = "Lnet/minecraft/world/item/enchantment/Enchantment;getMaxLevel()I"))
    private int bypassLevelCapForSuperBooks(Enchantment enchantment) {
        if (isSuperEnchantedBook(getAdditionSlot().getItem())) {
            return 255;
        }
        return enchantment.getMaxLevel();
    }

    @Unique
    private Slot getAdditionSlot() {
        return ((AnvilMenu) (Object) this).getSlot(1);
    }

    @Unique
    private static boolean isSuperEnchantedBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchants.entrySet()) {
            if (entry.getIntValue() > entry.getKey().value().getMaxLevel()) {
                return true;
            }
        }
        return false;
    }
}
