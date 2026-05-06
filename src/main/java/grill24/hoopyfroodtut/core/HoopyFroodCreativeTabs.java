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
                output.accept(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get());
                output.accept(HoopyFroodItems.INFINITE_IMPROBABILITY_DRIVE_ITEM.get());
                output.accept(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get());
                output.accept(HoopyFroodItems.SOMEBODY_ELSES_PROBLEM_FIELD_ITEM.get());
                output.accept(HoopyFroodItems.WOBBLY_WATER_BUCKET.get());
                output.accept(HoopyFroodItems.WOBBLY_LAVA_BUCKET.get());
                output.accept(HoopyFroodItems.WOBBLY_SLIME_BUCKET.get());
                output.accept(HoopyFroodItems.WOBBLY_HONEY_BUCKET.get());
                output.accept(HoopyFroodItems.WOBBLY_MAGMA_BUCKET.get());
                output.accept(HoopyFroodItems.INVERTER_ITEM.get());
                output.accept(HoopyFroodItems.PULSE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SLUGGISH_PULSE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.RELEASE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SLUGGISH_RELEASE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.REDSTONE_CLOCK_ITEM.get());
                output.accept(HoopyFroodItems.SLUGGISH_REDSTONE_CLOCK_ITEM.get());
                output.accept(HoopyFroodItems.PROXIMITY_SENSOR_ITEM.get());
                output.accept(HoopyFroodItems.EJECTOR_ITEM.get());
                output.accept(HoopyFroodItems.EXPELLER_ITEM.get());
                output.accept(HoopyFroodItems.BANISHING_BIN_ITEM.get());
                output.accept(HoopyFroodItems.MAGIC_MIRROR.get());
                output.accept(HoopyFroodItems.ROUND_TRIP_MAGIC_MIRROR.get());
                // Scaffolded redstone components
                output.accept(HoopyFroodItems.SCAFFOLDED_INVERTER_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_REPEATER_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_COMPARATOR_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_PULSE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_SLUGGISH_PULSE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_RELEASE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_SLUGGISH_RELEASE_LATCH_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_REDSTONE_CLOCK_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_REDSTONE_DUST_ITEM.get());
                // Angled repeaters
                output.accept(HoopyFroodItems.LEFT_ANGLED_REPEATER_ITEM.get());
                output.accept(HoopyFroodItems.RIGHT_ANGLED_REPEATER_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_LEFT_ANGLED_REPEATER_ITEM.get());
                output.accept(HoopyFroodItems.SCAFFOLDED_RIGHT_ANGLED_REPEATER_ITEM.get());
                // Inert TNT
                output.accept(HoopyFroodItems.INERT_TNT_ITEM.get());
                // Sturdy Pistons
                output.accept(HoopyFroodItems.STURDY_PISTON_ITEM.get());
                output.accept(HoopyFroodItems.STICKY_STURDY_PISTON_ITEM.get());
            }).build());

    // Add the example block item to the building blocks tab
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(HoopyFroodItems.BROWN_BRICKS_ITEM.get());
            event.accept(HoopyFroodItems.BROWN_BRICK.get());
        }
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(HoopyFroodItems.STURDY_PISTON_ITEM.get());
            event.accept(HoopyFroodItems.STICKY_STURDY_PISTON_ITEM.get());
        }
    }
}
