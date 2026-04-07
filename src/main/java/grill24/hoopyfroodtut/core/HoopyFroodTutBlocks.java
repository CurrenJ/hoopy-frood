package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.block.BeggingItemScrabbler;
import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.block.InfiniteImprobabilityDrive;
import grill24.hoopyfroodtut.block.PersonalPrivateItemPresenter;
import grill24.hoopyfroodtut.block.SomebodyElsesProblemField;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

    // A redstone-activated block that mines forward, places a successor with one fewer charge, then breaks.
    public static final DeferredBlock<DisposableCaterpillar> DISPOSABLE_CATERPILLAR = BLOCKS.registerBlock(
            "disposable_caterpillar",
            p -> new DisposableCaterpillar(p.mapColor(MapColor.COLOR_GREEN).noOcclusion()));

    // A machine that converts any inserted item into a completely random item from the entire game registry.
    public static final DeferredBlock<InfiniteImprobabilityDrive> INFINITE_IMPROBABILITY_DRIVE = BLOCKS.registerBlock(
            "infinite_improbability_drive",
            p -> new InfiniteImprobabilityDrive(p.strength(3.0F, 6.0F).noOcclusion()));

    // An autonomous item-collector that crawls toward dropped items and picks them up. Requires metallic nuggets as fuel.
    public static final DeferredBlock<BeggingItemScrabbler> BEGGING_ITEM_SCRABBLER = BLOCKS.registerBlock(
            "begging_item_scrabbler",
            p -> new BeggingItemScrabbler(p.mapColor(MapColor.COLOR_BROWN)
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(1.0F, 6.0F)
            ));

    // A single-item display block with an animated fluid surface that hides the item from other players.
    public static final DeferredBlock<PersonalPrivateItemPresenter> PERSONAL_PRIVATE_ITEM_PRESENTER = BLOCKS.registerBlock(
            "personal_private_item_presenter",
            p -> new PersonalPrivateItemPresenter(p
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(2.0f, 6.0f)));

    // A field emitter that makes the surrounding area invisible to mob AI and pathfinding.
    public static final DeferredBlock<SomebodyElsesProblemField> SOMEBODY_ELSES_PROBLEM_FIELD = BLOCKS.registerBlock(
            "somebody_elses_problem_field",
            p -> new SomebodyElsesProblemField(p
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(3.0f, 6.0f)
                    .lightLevel(state -> 5)));

    // ---- Block Families -----
    public static final Lazy<BlockFamily> BROWN_BRICKS_FAMILY = Lazy.lazy(() -> new BlockFamily.Builder(HoopyFroodTutBlocks.BROWN_BRICKS.get())
            .slab(HoopyFroodTutBlocks.BROWN_BRICKS_SLAB.get())
            .stairs(HoopyFroodTutBlocks.BROWN_BRICKS_STAIRS.get())
            .generateStonecutterRecipe()
            .getFamily()
    );
}
