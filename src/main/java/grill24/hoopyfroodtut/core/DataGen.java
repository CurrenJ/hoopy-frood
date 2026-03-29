package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.datagen.BlockModelProvider;
import grill24.hoopyfroodtut.datagen.RecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = HoopyFroodTut.MODID)
public class DataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.addProvider(new BlockModelProvider(event.getGenerator().getPackOutput()));
        event.addProvider(new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }
}
