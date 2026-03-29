package grill24.hoopyfroodtut.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodTutBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "hoopyfroodtut" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HoopyFroodTut.MODID);
    // Creates a new Block with the id "hoopyfroodtut:example_block", combining the namespace and path
    public static final DeferredBlock<Block> BROWN_BRICKS = BLOCKS.registerSimpleBlock("brown_bricks", p -> p.mapColor(MapColor.STONE));
}
