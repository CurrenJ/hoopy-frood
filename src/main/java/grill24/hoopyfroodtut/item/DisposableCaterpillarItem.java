package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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

    /** Returns whether this caterpillar has self-preservation instincts. */
    public static boolean isUndying(ItemStack stack) {
        return stack.getOrDefault(HoopyFroodDataComponents.CATERPILLAR_UNDYING.get(), false);
    }

    /** Returns the enchantments stored on this stack. */
    public static ItemEnchantments getEnchantments(ItemStack stack) {
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /**
     * Merges a list of caterpillar stacks into one:
     * <ul>
     *   <li>Integer components (charges, torches) are summed.</li>
     *   <li>Boolean components (immobile, undying) are OR-ed — a flag is kept if
     *       <em>any</em> input carries it, so properties are never silently lost.</li>
     *   <li>Enchantments are merged by taking the <em>maximum level</em> of each
     *       enchantment across all inputs, so no enchantment is ever lost.</li>
     * </ul>
     * The first stack is used as the base copy to preserve any unrecognised components.
     */
    public static ItemStack merge(Iterable<ItemStack> stacks) {
        ItemStack first = ItemStack.EMPTY;
        int totalCharges = 0;
        int totalTorches = 0;
        boolean anyImmobile = false;
        boolean anyUndying = false;
        ItemEnchantments.Mutable mergedEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        for (ItemStack stack : stacks) {
            if (first.isEmpty()) first = stack;
            totalCharges += getCharges(stack);
            totalTorches += getTorches(stack);
            anyImmobile |= isImmobile(stack);
            anyUndying  |= isUndying(stack);
            ItemEnchantments stackEnchants = getEnchantments(stack);
            for (Holder<Enchantment> ench : stackEnchants.keySet()) {
                mergedEnchantments.upgrade(ench, stackEnchants.getLevel(ench));
            }
        }

        ItemStack result = first.copyWithCount(1);
        result.set(HoopyFroodDataComponents.CATERPILLAR_CHARGES.get(), totalCharges);
        if (totalTorches > 0) result.set(HoopyFroodDataComponents.CATERPILLAR_TORCHES.get(), totalTorches);
        else result.remove(HoopyFroodDataComponents.CATERPILLAR_TORCHES.get());
        if (anyImmobile) result.set(HoopyFroodDataComponents.CATERPILLAR_IMMOBILE.get(), true);
        else result.remove(HoopyFroodDataComponents.CATERPILLAR_IMMOBILE.get());
        if (anyUndying) result.set(HoopyFroodDataComponents.CATERPILLAR_UNDYING.get(), true);
        else result.remove(HoopyFroodDataComponents.CATERPILLAR_UNDYING.get());
        ItemEnchantments finalEnchantments = mergedEnchantments.toImmutable();
        if (!finalEnchantments.isEmpty()) {
            result.set(DataComponents.ENCHANTMENTS, finalEnchantments);
        } else {
            result.remove(DataComponents.ENCHANTMENTS);
        }
        return result;
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
                be.populateFromItemStack(context.getItemInHand());
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
            if (isUndying(stack)) {
                components.accept(Component.translatable("item.hoopyfroodtut.disposable_caterpillar.undying")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        });
    }
}
