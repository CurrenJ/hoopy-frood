package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import grill24.hoopyfroodtut.blockentity.InfiniteImprobabilityDriveBlockEntity;
import grill24.hoopyfroodtut.client.renderer.BeggingItemScrabblerRenderer;
import grill24.hoopyfroodtut.client.renderer.DisposableCaterpillarRenderer;
import grill24.hoopyfroodtut.client.renderer.InfiniteImprobabilityDriveRenderer;
import grill24.hoopyfroodtut.client.renderer.SomebodyElsesProblemFieldRenderer;
import grill24.hoopyfroodtut.core.SepFieldManager;
import grill24.hoopyfroodtut.item.PerilSensitiveSunglassesItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@Mod(value = HoopyFroodTut.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = HoopyFroodTut.MODID, value = Dist.CLIENT)
public class HoopyFroodTutClient {
    public HoopyFroodTutClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(HoopyFroodTutClient::onRegisterRenderers);
        modEventBus.addListener(HoopyFroodTutClient::onRegisterAdditionalModels);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                HoopyFroodBlockEntityTypes.DISPOSABLE_CATERPILLAR.get(),
                DisposableCaterpillarRenderer::new);
        event.registerBlockEntityRenderer(
                HoopyFroodBlockEntityTypes.BEGGING_ITEM_SCRABBLER.get(),
                BeggingItemScrabblerRenderer::new);
        event.registerBlockEntityRenderer(
                HoopyFroodBlockEntityTypes.INFINITE_IMPROBABILITY_DRIVE.get(),
                InfiniteImprobabilityDriveRenderer::new);
        event.registerBlockEntityRenderer(
                HoopyFroodBlockEntityTypes.SOMEBODY_ELSES_PROBLEM_FIELD.get(),
                SomebodyElsesProblemFieldRenderer::new);
    }

    private static void onRegisterAdditionalModels(ModelEvent.RegisterStandalone event) {
        event.register(DisposableCaterpillarRenderer.FULL_MODEL_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/disposable_caterpillar_full")));

        event.register(DisposableCaterpillarRenderer.BASE_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/disposable_caterpillar_active")));
        for (int i = 0; i < 10; i++) {
            event.register(DisposableCaterpillarRenderer.SEGMENT_KEYS[i],
                    SimpleUnbakedStandaloneModel.simpleModelWrapper(
                            Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                    "block/disposable_caterpillar_seg" + i)));
        }
        event.register(DisposableCaterpillarRenderer.LEGS_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/disposable_caterpillar_legs")));

        event.register(BeggingItemScrabblerRenderer.BODY_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/begging_item_scrabbler_body")));
        event.register(BeggingItemScrabblerRenderer.HEAD_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/begging_item_scrabbler_head")));
        event.register(BeggingItemScrabblerRenderer.BODY_DISABLED_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/begging_item_scrabbler_body_disabled")));
        event.register(BeggingItemScrabblerRenderer.HEAD_DISABLED_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/begging_item_scrabbler_head_disabled")));

        event.register(InfiniteImprobabilityDriveRenderer.BASE_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/infinite_improbability_drive_base")));
        event.register(InfiniteImprobabilityDriveRenderer.STAMP_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/infinite_improbability_drive_stamp")));
        event.register(InfiniteImprobabilityDriveRenderer.WHEEL_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/infinite_improbability_drive_wheel")));
        event.register(InfiniteImprobabilityDriveRenderer.SUPPORT_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "block/infinite_improbability_drive_support")));

        event.register(SomebodyElsesProblemFieldRenderer.INNER_RING_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/sep_field_inner_ring")));
        event.register(SomebodyElsesProblemFieldRenderer.MID_RING_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/sep_field_mid_ring")));
        event.register(SomebodyElsesProblemFieldRenderer.OUTER_RING_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/sep_field_outer_ring")));
        event.register(SomebodyElsesProblemFieldRenderer.FLOATER_KEY,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/sep_field_floater")));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        HoopyFroodTut.LOGGER.info("HELLO FROM CLIENT SETUP");
        HoopyFroodTut.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    // -------------------------------------------------------------------------
    // Item tooltips — for items registered without a custom BlockItem subclass
    // -------------------------------------------------------------------------

    @SubscribeEvent
    static void onItemTooltip(ItemTooltipEvent event) {
        Item item = event.getItemStack().getItem();
        List<Component> tip = event.getToolTip();

        if (item == HoopyFroodItems.BALANCER_NODE_ITEM.get()) {
            tip.add(1, Component.translatable("item.hoopyfroodtut.balancer_node.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            tip.add(2, Component.empty()
                    .append(Component.literal("Range: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(BalancerNodeBlockEntity.MAX_RANGE)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" blocks base  (+").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(BalancerNodeBlockEntity.RANGE_PER_EXTENDER)).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" per Range Extender)").withStyle(ChatFormatting.GRAY)));
            tip.add(3, Component.translatable("item.hoopyfroodtut.balancer_node.tooltip.hint")
                    .withStyle(ChatFormatting.DARK_GRAY));

        } else if (item == HoopyFroodItems.INFINITE_IMPROBABILITY_DRIVE_ITEM.get()) {
            tip.add(1, Component.translatable("item.hoopyfroodtut.infinite_improbability_drive.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            tip.add(2, Component.empty()
                    .append(Component.literal("Base: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(InfiniteImprobabilityDriveBlockEntity.BASE_CONVERSION_TICKS / 20 + " s").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("  →  min ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(InfiniteImprobabilityDriveBlockEntity.MIN_CONVERSION_TICKS / 20 + " s").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("  |  scan radius: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(InfiniteImprobabilityDriveBlockEntity.SCAN_RADIUS)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" blocks").withStyle(ChatFormatting.GRAY)));
            tip.add(3, Component.translatable("item.hoopyfroodtut.infinite_improbability_drive.tooltip.hint")
                    .withStyle(ChatFormatting.DARK_GRAY));

        } else if (item == HoopyFroodItems.SOMEBODY_ELSES_PROBLEM_FIELD_ITEM.get()) {
            tip.add(1, Component.translatable("item.hoopyfroodtut.somebody_elses_problem_field.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            tip.add(2, Component.empty()
                    .append(Component.literal("Radius: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(SepFieldManager.FIELD_RADIUS)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" blocks").withStyle(ChatFormatting.GRAY)));

        } else if (item == HoopyFroodItems.BROWN_BRICK.get()) {
            tip.add(1, Component.translatable("item.hoopyfroodtut.brown_brick.tooltip.desc")
                    .withStyle(ChatFormatting.GRAY));
            tip.add(2, Component.empty()
                    .append(Component.literal("Nutrition: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("1").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal("  |  Saturation: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("2").withStyle(ChatFormatting.YELLOW)));
        }
    }

    /**
     * Filters out hostile mob rendering when the local player is wearing Peril Sensitive Sunglasses.
     * Tamed mobs and other players remain visible.
     */
    @SubscribeEvent
    static void onRenderLivingEntity(RenderLivingEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player localPlayer = minecraft.player;

        if (localPlayer == null) {
            return;
        }

        // Only filter if the local player is wearing the glasses in the head slot
        ItemStack headArmor = localPlayer.getItemBySlot(EquipmentSlot.HEAD);
        if (headArmor.isEmpty() || !(headArmor.getItem() instanceof PerilSensitiveSunglassesItem)) {
            return;
        }

        // Get the entity being rendered - use the renderer's entity field if getEntity not available
        if (event.getRenderState() instanceof LivingEntityRenderState renderState) {
            EntityType<?> entityType = renderState.entityType;
            if (entityType.getCategory() == MobCategory.MONSTER) {
                event.setCanceled(true);
            }
        }
    }
}
