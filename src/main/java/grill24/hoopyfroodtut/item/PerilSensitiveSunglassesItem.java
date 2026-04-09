package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.core.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Peril Sensitive Sunglasses - a cosmetic head gear item that hides hostile mobs
 * from the wearer's view when rendered on the client.
 *
 * The item is made equippable via Item.Properties in registration
 * (head slot Equippable component).
 */
public class PerilSensitiveSunglassesItem extends Item {

    public PerilSensitiveSunglassesItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            components.accept(Component.translatable("item.hoopyfroodtut.peril_sensitive_sunglasses.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            components.accept(Component.translatable("item.hoopyfroodtut.peril_sensitive_sunglasses.tooltip.note")
                    .withStyle(ChatFormatting.DARK_GRAY));
        });
    }
}



