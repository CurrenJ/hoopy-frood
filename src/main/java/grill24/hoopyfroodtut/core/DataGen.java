package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.datagen.BlockLootTableProvider;
import grill24.hoopyfroodtut.datagen.BlockModelProvider;
import grill24.hoopyfroodtut.datagen.BlockTagProvider;
import grill24.hoopyfroodtut.datagen.RecipeProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = HoopyFroodTut.MODID)
public class DataGen {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.addProvider(new BlockModelProvider(event.getGenerator().getPackOutput()));
        event.addProvider(new BlockTagProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.addProvider(new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        event.addProvider(new LootTableProvider(
                event.getGenerator().getPackOutput(),
                Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootTableProvider::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()
        ));
    }
}
