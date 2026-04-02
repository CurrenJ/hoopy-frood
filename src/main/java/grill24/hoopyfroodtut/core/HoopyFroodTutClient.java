package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.client.renderer.BeggingItemScrabblerRenderer;
import grill24.hoopyfroodtut.client.renderer.DisposableCaterpillarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

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
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        HoopyFroodTut.LOGGER.info("HELLO FROM CLIENT SETUP");
        HoopyFroodTut.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
