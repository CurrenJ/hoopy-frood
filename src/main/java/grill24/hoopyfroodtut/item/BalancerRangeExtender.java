package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** Inserted into a Balancer Node to extend its scanning range. */
public class BalancerRangeExtender extends Item {
    public BalancerRangeExtender(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        components.accept(Component.translatable("item.hoopyfroodtut.balancer_range_extender.tooltip.desc")
                .withStyle(ChatFormatting.GRAY));
        components.accept(Component.empty()
                .append(Component.literal("Right-click a node to insert  ·  ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("+" + BalancerNodeBlockEntity.RANGE_PER_EXTENDER + " block").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" scan range per extender").withStyle(ChatFormatting.GRAY)));
    }
}
