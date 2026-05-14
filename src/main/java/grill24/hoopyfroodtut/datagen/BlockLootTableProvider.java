package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

public class BlockLootTableProvider extends BlockLootSubProvider {

    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    /** Drops 1x scaffolding + 1x baseItem when the scaffolded block is broken. */
    private LootTable.Builder dropScaffolded(Block scaffoldedBlock, ItemLike baseItem) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.SCAFFOLDING)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(baseItem)));
    }

    @Override
    protected void generate() {
        dropSelf(HoopyFroodTutBlocks.BALANCER_NODE.get());
        dropSelf(HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get());
        dropSelf(HoopyFroodTutBlocks.INVERTER.get());
        dropSelf(HoopyFroodTutBlocks.PULSE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.RELEASE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get());
        dropSelf(HoopyFroodTutBlocks.REDSTONE_CLOCK.get());
        dropSelf(HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get());
        dropSelf(HoopyFroodTutBlocks.PROXIMITY_SENSOR.get());
        add(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get())
                                        .apply(CopyComponentsFunction
                                                .copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                                .include(HoopyFroodDataComponents.LAVA_NEUTRALIZER_CHARGES.get())))));
        dropSelf(HoopyFroodTutBlocks.EJECTOR.get());
        dropSelf(HoopyFroodTutBlocks.EXPELLER.get());
        add(HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get(), noDrop());
        add(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(), noDrop());
        // Scaffolded redstone components — drop scaffolding + base component
        add(HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get(), HoopyFroodTutBlocks.INVERTER.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get(), Items.REPEATER));
        add(HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get(), Items.COMPARATOR));
        add(HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get(), HoopyFroodTutBlocks.PULSE_LATCH.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get(), HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get(), HoopyFroodTutBlocks.RELEASE_LATCH.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get(), HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get(), HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get(), HoopyFroodTutBlocks.REDSTONE_CLOCK.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get(), Items.REDSTONE));
        // Angled repeaters
        dropSelf(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get());
        dropSelf(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get());
        dropSelf(HoopyFroodTutBlocks.INERT_TNT.get());
        dropSelf(HoopyFroodTutBlocks.STURDY_PISTON.get());
        dropSelf(HoopyFroodTutBlocks.STICKY_STURDY_PISTON.get());
        add(HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get(), noDrop());
        add(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get(), HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get()));
        add(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get(),
                dropScaffolded(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get(), HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                HoopyFroodTutBlocks.BALANCER_NODE.get(),
                HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get(),
                HoopyFroodTutBlocks.INVERTER.get(),
                HoopyFroodTutBlocks.PULSE_LATCH.get(),
                HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get(),
                HoopyFroodTutBlocks.RELEASE_LATCH.get(),
                HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get(),
                HoopyFroodTutBlocks.REDSTONE_CLOCK.get(),
                HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get(),
                HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get(),
                HoopyFroodTutBlocks.PROXIMITY_SENSOR.get(),
                HoopyFroodTutBlocks.EJECTOR.get(),
                HoopyFroodTutBlocks.EXPELLER.get(),
                HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get(),
                HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(),
                // Scaffolded redstone components
                HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get(),
                // Angled repeaters
                HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get(),
                HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get(),
                HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get(),
                HoopyFroodTutBlocks.INERT_TNT.get(),
                HoopyFroodTutBlocks.STURDY_PISTON.get(),
                HoopyFroodTutBlocks.STICKY_STURDY_PISTON.get(),
                HoopyFroodTutBlocks.STURDY_PISTON_HEAD.get()
        );
    }
}
