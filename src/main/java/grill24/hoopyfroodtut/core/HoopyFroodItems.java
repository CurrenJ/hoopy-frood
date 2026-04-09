package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.item.BalancerRangeExtender;
import grill24.hoopyfroodtut.item.BeggingItemScrabblerItem;
import grill24.hoopyfroodtut.item.DisposableCaterpillarItem;
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
                    p -> new DisposableCaterpillarItem(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(), p));

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
                    p -> new WobblyWaterBucketItem(p.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<BlockItem> BANISHING_BIN_ITEM =
            ITEMS.registerSimpleBlockItem("banishing_bin", HoopyFroodTutBlocks.BANISHING_BIN);

    public static final DeferredItem<PerilSensitiveSunglassesItem> PERIL_SENSITIVE_SUNGLASSES =
            ITEMS.registerItem("peril_sensitive_sunglasses",
                    p -> new PerilSensitiveSunglassesItem(
                            p.stacksTo(1).equippable(EquipmentSlot.HEAD)));
}
