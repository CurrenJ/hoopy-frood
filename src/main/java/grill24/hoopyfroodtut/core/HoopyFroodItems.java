package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.item.BalancerRangeExtender;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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
}
