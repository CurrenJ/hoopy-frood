package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends BlockTagsProvider {
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, HoopyFroodTut.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(HoopyFroodTutBlocks.BROWN_BRICKS.get())
                .add(HoopyFroodTutBlocks.BROWN_BRICKS_SLAB.get())
                .add(HoopyFroodTutBlocks.BROWN_BRICKS_STAIRS.get())
                .add(HoopyFroodTutBlocks.BALANCER_NODE.get())
                .add(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get())
                .add(HoopyFroodTutBlocks.INFINITE_IMPROBABILITY_DRIVE.get())
                .add(HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get())
                .add(HoopyFroodTutBlocks.WOBBLY_WATER.get())
                .add(HoopyFroodTutBlocks.BANISHING_BIN.get())
                .add(HoopyFroodTutBlocks.INVERTER.get())
                .add(HoopyFroodTutBlocks.PULSE_LATCH.get())
                .add(HoopyFroodTutBlocks.RELEASE_LATCH.get())
                .add(HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get())
                .add(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get())
                .add(HoopyFroodTutBlocks.REDSTONE_CLOCK.get())
                .add(HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get())
                .add(HoopyFroodTutBlocks.PROXIMITY_SENSOR.get())
                .add(HoopyFroodTutBlocks.EJECTOR.get())
                .add(HoopyFroodTutBlocks.EXPELLER.get())
                .add(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get())
                .add(HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get())
                .add(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get())
                .add(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get())
                .add(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get())
                .add(HoopyFroodTutBlocks.INERT_TNT.get())
                .add(HoopyFroodTutBlocks.STURDY_PISTON.get())
                .add(HoopyFroodTutBlocks.STICKY_STURDY_PISTON.get())
                .add(HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(HoopyFroodTutBlocks.EJECTOR.get())
                .add(HoopyFroodTutBlocks.EXPELLER.get())
                .add(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get());
    }
}
