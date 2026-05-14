package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class LavaNeutralizerItem extends BlockItem {

    public LavaNeutralizerItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            Component rechargeName = getRechargeItemName();
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.lava_neutralizer.tooltip.desc", rechargeName)
                    .withStyle(ChatFormatting.GRAY));
            int charges = stack.getOrDefault(HoopyFroodDataComponents.LAVA_NEUTRALIZER_CHARGES.get(), 4);
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.lava_neutralizer.charges", charges)
                    .withStyle(ChatFormatting.GRAY));
        });
    }

    private static Component getRechargeItemName() {
        String id = Config.LAVA_NEUTRALIZER_RECHARGE_ITEM.get();
        Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
        return item != null
                ? Component.translatable(item.getDescriptionId())
                : Component.literal(id);
    }
}
