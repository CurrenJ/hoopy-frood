package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class BlockLootTableProvider extends BlockLootSubProvider {

    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(HoopyFroodTutBlocks.BALANCER_NODE.get());
        dropSelf(HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get());
        dropSelf(HoopyFroodTutBlocks.INVERTER.get());
        dropSelf(HoopyFroodTutBlocks.PULSE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.REDSTONE_CLOCK.get());
        dropSelf(HoopyFroodTutBlocks.PROXIMITY_SENSOR.get());
        dropSelf(HoopyFroodTutBlocks.EJECTOR.get());
        dropSelf(HoopyFroodTutBlocks.EXPELLER.get());
        add(HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get(), noDrop());
        add(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                HoopyFroodTutBlocks.BALANCER_NODE.get(),
                HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get(),
                HoopyFroodTutBlocks.INVERTER.get(),
                HoopyFroodTutBlocks.PULSE_LATCH.get(),
                HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get(),
                HoopyFroodTutBlocks.REDSTONE_CLOCK.get(),
                HoopyFroodTutBlocks.PROXIMITY_SENSOR.get(),
                HoopyFroodTutBlocks.EJECTOR.get(),
                HoopyFroodTutBlocks.EXPELLER.get(),
                HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get(),
                HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get()
        );
    }
}
