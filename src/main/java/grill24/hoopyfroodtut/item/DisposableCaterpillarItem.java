package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

/**
 * BlockItem for the Disposable Caterpillar.
 * <p>
 * Transfers the {@code hoopyfroodtut:caterpillar_charges} data component from the
 * item to the block entity when the block is placed, and shows the charge count
 * in the item tooltip.
 */
public class DisposableCaterpillarItem extends BlockItem {

    public DisposableCaterpillarItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /** Returns the charge count stored on this stack (defaults to 1 if absent). */
    public static int getCharges(ItemStack stack) {
        return stack.getOrDefault(HoopyFroodDataComponents.CATERPILLAR_CHARGES.get(), 1);
    }

    /** Returns the torch count stored on this stack (defaults to 0 if absent). */
    public static int getTorches(ItemStack stack) {
        return stack.getOrDefault(HoopyFroodDataComponents.CATERPILLAR_TORCHES.get(), 0);
    }

    /** Returns whether this caterpillar is immobile (stuck). Defaults to false if absent. */
    public static boolean isImmobile(ItemStack stack) {
        return stack.getOrDefault(HoopyFroodDataComponents.CATERPILLAR_IMMOBILE.get(), false);
    }

    // -------------------------------------------------------------------------
    // Placement — push charges from item into the new block entity
    // -------------------------------------------------------------------------

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && !context.getLevel().isClientSide()) {
            var pos = context.getClickedPos();
            if (context.getLevel().getBlockEntity(pos) instanceof DisposableCaterpillarBlockEntity be) {
                be.setCharges(getCharges(context.getItemInHand()));
                be.setTorches(getTorches(context.getItemInHand()));
                be.setImmobile(isImmobile(context.getItemInHand()));
            }
        }
        return placed;
    }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            components.accept(Component.translatable("item.hoopyfroodtut.disposable_caterpillar.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            int charges = getCharges(stack);
            components.accept(Component.translatable("item.hoopyfroodtut.disposable_caterpillar.charges", charges)
                    .withStyle(ChatFormatting.GRAY));
            int torches = getTorches(stack);
            if (torches > 0) {
                components.accept(Component.translatable("item.hoopyfroodtut.disposable_caterpillar.torches", torches)
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (isImmobile(stack)) {
                components.accept(Component.translatable("item.hoopyfroodtut.disposable_caterpillar.stuck")
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        });
    }
}
