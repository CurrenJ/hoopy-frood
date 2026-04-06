package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;

/**
 * BlockItem for the Begging Item Scrabbler.
 * <p>
 * Transfers the {@code hoopyfroodtut:scrabbler_nuggets} and
 * {@code hoopyfroodtut:scrabbler_home} data components from the item to the block
 * entity when placed, and shows fuel/home status in the item tooltip.
 * <p>
 * Shift+right-clicking a container block while holding this item pairs the item
 * with that container — stored as the {@code scrabbler_home} component. The pairing
 * is preserved when the scrabbler is picked up and re-placed.
 */
public class BeggingItemScrabblerItem extends BlockItem {

    public BeggingItemScrabblerItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static int getNuggets(ItemStack stack) {
        return stack.getOrDefault(HoopyFroodDataComponents.SCRABBLER_NUGGETS.get(), 0);
    }

    // -------------------------------------------------------------------------
    // Shift+right-click on a container — link this item to that container
    // -------------------------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            // Client: suppress block placement so no ghost block appears
            if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

            Level level = context.getLevel();
            BlockPos clicked = context.getClickedPos();

            // Check null face and all cardinal faces for an item handler
            boolean foundContainer = false;
            ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, clicked, null);
            if (h != null) {
                foundContainer = true;
            } else {
                for (Direction face : Direction.values()) {
                    if (level.getCapability(Capabilities.Item.BLOCK, clicked, face) != null) {
                        foundContainer = true;
                        break;
                    }
                }
            }

            if (foundContainer) {
                context.getItemInHand().set(HoopyFroodDataComponents.SCRABBLER_HOME.get(), clicked);
                player.sendSystemMessage(
                        Component.translatable("item.hoopyfroodtut.begging_item_scrabbler.home_set"));
            } else {
                player.sendSystemMessage(
                        Component.translatable("item.hoopyfroodtut.begging_item_scrabbler.no_container"));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    // -------------------------------------------------------------------------
    // Placement — push nuggets and home pos from item into the new block entity
    // -------------------------------------------------------------------------

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && !context.getLevel().isClientSide()) {
            BlockPos pos = context.getClickedPos();
            if (context.getLevel().getBlockEntity(pos) instanceof BeggingItemScrabblerBlockEntity be) {
                int nuggets = getNuggets(context.getItemInHand());
                if (nuggets > 0) be.addNuggets(nuggets);
                BlockPos home = context.getItemInHand().get(HoopyFroodDataComponents.SCRABBLER_HOME.get());
                if (home != null) be.setHomePos(home);
                int slots = context.getItemInHand().getOrDefault(HoopyFroodDataComponents.SCRABBLER_SLOTS.get(),
                        BeggingItemScrabblerBlockEntity.DEFAULT_INVENTORY_SIZE);
                if (slots > BeggingItemScrabblerBlockEntity.DEFAULT_INVENTORY_SIZE) be.setInventorySize(slots);
            }
        }
        return placed;
    }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        components.accept(Component.translatable("item.hoopyfroodtut.begging_item_scrabbler.tooltip.desc")
                .withStyle(ChatFormatting.GRAY));

        int nuggets = getNuggets(stack);
        if (nuggets > 0) {
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.begging_item_scrabbler.nuggets", nuggets)
                    .withStyle(ChatFormatting.GRAY));
        } else {
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.begging_item_scrabbler.no_fuel")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        int slots = stack.getOrDefault(HoopyFroodDataComponents.SCRABBLER_SLOTS.get(),
                BeggingItemScrabblerBlockEntity.DEFAULT_INVENTORY_SIZE);
        components.accept(Component.translatable(
                "item.hoopyfroodtut.begging_item_scrabbler.slots", slots)
                .withStyle(ChatFormatting.GRAY));

        BlockPos home = stack.get(HoopyFroodDataComponents.SCRABBLER_HOME.get());
        if (home != null) {
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.begging_item_scrabbler.home_linked",
                    home.getX(), home.getY(), home.getZ())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            components.accept(Component.translatable(
                    "item.hoopyfroodtut.begging_item_scrabbler.no_home")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
