package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.block.WobblyWater;
import grill24.hoopyfroodtut.item.BalancerRangeExtender;
import grill24.hoopyfroodtut.item.BeggingItemScrabblerItem;
import grill24.hoopyfroodtut.item.DisposableCaterpillarItem;
import grill24.hoopyfroodtut.item.LavaNeutralizerItem;
import grill24.hoopyfroodtut.item.PerilSensitiveSunglassesItem;
import grill24.hoopyfroodtut.item.WobblyWaterBucketItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodItems {
    // Create a Deferred Register to hold Items which will all be registered under the "hoopyfroodtut" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HoopyFroodTut.MODID);

    // Creates a new food item with the id "hoopyfroodtut:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> BROWN_BRICK = ITEMS.registerSimpleItem("brown_brick", p -> p.food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    // Creates a new BlockItem with the id "hoopyfroodtut:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> BROWN_BRICKS_ITEM = ITEMS.registerSimpleBlockItem("brown_bricks", HoopyFroodTutBlocks.BROWN_BRICKS);
    public static final DeferredItem<BlockItem> BROWN_BRICKS_SLAB_ITEM = ITEMS.registerSimpleBlockItem("brown_bricks_slab", HoopyFroodTutBlocks.BROWN_BRICKS_SLAB);
    public static final DeferredItem<BlockItem> BROWN_BRICKS_STAIRS_ITEM = ITEMS.registerSimpleBlockItem("brown_bricks_stairs", HoopyFroodTutBlocks.BROWN_BRICKS_STAIRS);

    public static final DeferredItem<BlockItem> BALANCER_NODE_ITEM = ITEMS.registerSimpleBlockItem("balancer_node", HoopyFroodTutBlocks.BALANCER_NODE);

    public static final DeferredItem<BalancerRangeExtender> BALANCER_RANGE_EXTENDER =
            ITEMS.registerItem("balancer_range_extender", BalancerRangeExtender::new);

    public static final DeferredItem<DisposableCaterpillarItem> DISPOSABLE_CATERPILLAR_ITEM =
            ITEMS.registerItem("disposable_caterpillar",
                    p -> new DisposableCaterpillarItem(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(), p.enchantable(14)));

    public static final DeferredItem<BeggingItemScrabblerItem> BEGGING_ITEM_SCRABBLER_ITEM =
            ITEMS.registerItem("begging_item_scrabbler",
                    p -> new BeggingItemScrabblerItem(HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get(), p));

    public static final DeferredItem<BlockItem> INFINITE_IMPROBABILITY_DRIVE_ITEM =
            ITEMS.registerSimpleBlockItem("infinite_improbability_drive",
                    HoopyFroodTutBlocks.INFINITE_IMPROBABILITY_DRIVE);

    public static final DeferredItem<BlockItem> SOMEBODY_ELSES_PROBLEM_FIELD_ITEM =
            ITEMS.registerSimpleBlockItem("somebody_elses_problem_field",
                    HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD);

    public static final DeferredItem<BlockItem> WOBBLY_WATER_ITEM =
            ITEMS.registerSimpleBlockItem("wobbly_water",
                    HoopyFroodTutBlocks.WOBBLY_WATER);

    public static final DeferredItem<WobblyWaterBucketItem> WOBBLY_WATER_BUCKET =
            ITEMS.registerItem("wobbly_water_bucket",
                    p -> new WobblyWaterBucketItem(WobblyWater.SurfaceTexture.WATER, p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<WobblyWaterBucketItem> WOBBLY_LAVA_BUCKET =
            ITEMS.registerItem("wobbly_lava_bucket",
                    p -> new WobblyWaterBucketItem(WobblyWater.SurfaceTexture.LAVA, p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<WobblyWaterBucketItem> WOBBLY_SLIME_BUCKET =
            ITEMS.registerItem("wobbly_slime_bucket",
                    p -> new WobblyWaterBucketItem(WobblyWater.SurfaceTexture.SLIME, p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<WobblyWaterBucketItem> WOBBLY_HONEY_BUCKET =
            ITEMS.registerItem("wobbly_honey_bucket",
                    p -> new WobblyWaterBucketItem(WobblyWater.SurfaceTexture.HONEY, p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<WobblyWaterBucketItem> WOBBLY_MAGMA_BUCKET =
            ITEMS.registerItem("wobbly_magma_bucket",
                    p -> new WobblyWaterBucketItem(WobblyWater.SurfaceTexture.MAGMA, p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BlockItem> INVERTER_ITEM =
            ITEMS.registerSimpleBlockItem("inverter", HoopyFroodTutBlocks.INVERTER);

    public static final DeferredItem<BlockItem> PULSE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("pulse_latch", HoopyFroodTutBlocks.PULSE_LATCH);

    public static final DeferredItem<BlockItem> RELEASE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("release_latch", HoopyFroodTutBlocks.RELEASE_LATCH);

    public static final DeferredItem<BlockItem> SLUGGISH_RELEASE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("sluggish_release_latch", HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH);

    public static final DeferredItem<BlockItem> SLUGGISH_PULSE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("sluggish_pulse_latch", HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH);

    public static final DeferredItem<BlockItem> REDSTONE_CLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("redstone_clock", HoopyFroodTutBlocks.REDSTONE_CLOCK);

    public static final DeferredItem<BlockItem> SLUGGISH_REDSTONE_CLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("sluggish_redstone_clock", HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK);

    public static final DeferredItem<BlockItem> PROXIMITY_SENSOR_ITEM =
            ITEMS.registerSimpleBlockItem("proximity_sensor", HoopyFroodTutBlocks.PROXIMITY_SENSOR);

    public static final DeferredItem<BlockItem> PRECISION_DISPENSER_ITEM =
            ITEMS.registerSimpleBlockItem("precision_dispenser", HoopyFroodTutBlocks.PRECISION_DISPENSER);

    public static final DeferredItem<BlockItem> EJECTOR_ITEM =
            ITEMS.registerSimpleBlockItem("ejector", HoopyFroodTutBlocks.EJECTOR);

    public static final DeferredItem<BlockItem> EXPELLER_ITEM =
            ITEMS.registerSimpleBlockItem("expeller", HoopyFroodTutBlocks.EXPELLER);

    public static final DeferredItem<LavaNeutralizerItem> LAVA_NEUTRALIZER_ITEM =
            ITEMS.registerItem("lava_neutralizer",
                    p -> new LavaNeutralizerItem(HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get(), p.fireResistant()));

    public static final DeferredItem<BlockItem> BANISHING_BIN_ITEM =
            ITEMS.registerSimpleBlockItem("banishing_bin", HoopyFroodTutBlocks.BANISHING_BIN);

    public static final DeferredItem<PerilSensitiveSunglassesItem> PERIL_SENSITIVE_SUNGLASSES =
            ITEMS.registerItem("peril_sensitive_sunglasses",
                    p -> new PerilSensitiveSunglassesItem(
                            p.stacksTo(1).equippable(EquipmentSlot.HEAD)));

    // ---- Scaffolded Redstone Components ----
    public static final DeferredItem<BlockItem> SCAFFOLDED_INVERTER_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_inverter", HoopyFroodTutBlocks.SCAFFOLDED_INVERTER);

    public static final DeferredItem<BlockItem> SCAFFOLDED_RELEASE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_release_latch", HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH);

    public static final DeferredItem<BlockItem> SCAFFOLDED_SLUGGISH_RELEASE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_sluggish_release_latch", HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH);

    public static final DeferredItem<BlockItem> SCAFFOLDED_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_repeater", HoopyFroodTutBlocks.SCAFFOLDED_REPEATER);

    public static final DeferredItem<BlockItem> SCAFFOLDED_COMPARATOR_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_comparator", HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR);

    public static final DeferredItem<BlockItem> SCAFFOLDED_PULSE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_pulse_latch", HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH);

    public static final DeferredItem<BlockItem> SCAFFOLDED_SLUGGISH_PULSE_LATCH_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_sluggish_pulse_latch", HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH);

    public static final DeferredItem<BlockItem> SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_sluggish_redstone_clock", HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK);

    public static final DeferredItem<BlockItem> SCAFFOLDED_REDSTONE_CLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_redstone_clock", HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK);

    public static final DeferredItem<BlockItem> SCAFFOLDED_REDSTONE_DUST_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_redstone_dust", HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST);

    // ---- Angled Repeaters ----
    public static final DeferredItem<BlockItem> LEFT_ANGLED_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("left_angled_repeater", HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER);

    public static final DeferredItem<BlockItem> RIGHT_ANGLED_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("right_angled_repeater", HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER);

    public static final DeferredItem<BlockItem> SCAFFOLDED_LEFT_ANGLED_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_left_angled_repeater", HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER);

    public static final DeferredItem<BlockItem> SCAFFOLDED_RIGHT_ANGLED_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("scaffolded_right_angled_repeater", HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER);

    public static final DeferredItem<BlockItem> INERT_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("inert_tnt", HoopyFroodTutBlocks.INERT_TNT);

    // Sturdy Pistons
    public static final DeferredItem<BlockItem> STURDY_PISTON_ITEM =
            ITEMS.registerSimpleBlockItem("sturdy_piston", HoopyFroodTutBlocks.STURDY_PISTON);

    public static final DeferredItem<BlockItem> STICKY_STURDY_PISTON_ITEM =
            ITEMS.registerSimpleBlockItem("sticky_sturdy_piston", HoopyFroodTutBlocks.STICKY_STURDY_PISTON);
}
