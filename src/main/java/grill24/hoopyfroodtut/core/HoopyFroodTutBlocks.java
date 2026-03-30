package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.block.BalancerNode;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.util.Lazy;

public class HoopyFroodTutBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "hoopyfroodtut" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HoopyFroodTut.MODID);
    // Creates a new Block with the id "hoopyfroodtut:example_block", combining the namespace and path
    public static final DeferredBlock<Block> BROWN_BRICKS = BLOCKS.registerSimpleBlock("brown_bricks", p -> p.mapColor(MapColor.STONE));
    public static final DeferredBlock<SlabBlock> BROWN_BRICKS_SLAB = BLOCKS.registerBlock("brown_bricks_slab", p -> new SlabBlock(p.mapColor(MapColor.STONE)));
    public static final DeferredBlock<StairBlock> BROWN_BRICKS_STAIRS = BLOCKS.registerBlock("brown_bricks_stairs", p -> new StairBlock(BROWN_BRICKS.get().defaultBlockState(), p.mapColor(MapColor.STONE)));

    // A small directional node that balances items from an attached source into a line of destinations.
    public static final DeferredBlock<BalancerNode> BALANCER_NODE = BLOCKS.registerBlock("balancer_node",
            p -> new BalancerNode(p.noOcclusion().noCollision()));

    // ---- Block Families -----
    public static final Lazy<BlockFamily> BROWN_BRICKS_FAMILY = Lazy.lazy(() -> new BlockFamily.Builder(HoopyFroodTutBlocks.BROWN_BRICKS.get())
            .slab(HoopyFroodTutBlocks.BROWN_BRICKS_SLAB.get())
            .stairs(HoopyFroodTutBlocks.BROWN_BRICKS_STAIRS.get())
            .generateStonecutterRecipe()
            .getFamily()
    );
}
