package grill24.hoopyfroodtut.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = HoopyFroodTut.MODID)
public class HoopyFroodCreativeTabs {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "hoopyfroodtut" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HoopyFroodTut.MODID);
    // Creates a creative tab with the id "hoopyfroodtut:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HFT_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hoopyfroodtut")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> HoopyFroodItems.BROWN_BRICK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(HoopyFroodItems.BROWN_BRICK.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(HoopyFroodItems.BROWN_BRICKS_ITEM.get());
                output.accept(HoopyFroodItems.BALANCER_NODE_ITEM.get());
                output.accept(HoopyFroodItems.BALANCER_RANGE_EXTENDER.get());
            }).build());

    // Add the example block item to the building blocks tab
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(HoopyFroodItems.BROWN_BRICKS_ITEM.get());
            event.accept(HoopyFroodItems.BROWN_BRICK.get());
        }
    }
}
