package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.block.AngledRepeater;
import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.block.BanishingBin;
import grill24.hoopyfroodtut.block.BeggingItemScrabbler;
import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.block.Ejector;
import grill24.hoopyfroodtut.block.Expeller;
import grill24.hoopyfroodtut.block.Inverter;
import grill24.hoopyfroodtut.block.ScaffoldedInverter;
import grill24.hoopyfroodtut.block.ScaffoldedSluggishRedstoneClock;
import grill24.hoopyfroodtut.block.SluggishRedstoneClock;
import grill24.hoopyfroodtut.block.LeftAngledRepeater;
import grill24.hoopyfroodtut.block.PulseLatch;
import grill24.hoopyfroodtut.block.RightAngledRepeater;
import grill24.hoopyfroodtut.block.ScaffoldedComparator;
import grill24.hoopyfroodtut.block.ScaffoldedLeftAngledRepeater;
import grill24.hoopyfroodtut.block.ScaffoldedPulseLatch;
import grill24.hoopyfroodtut.block.ScaffoldedRedstoneClock;
import grill24.hoopyfroodtut.block.ScaffoldedRedstoneDust;
import grill24.hoopyfroodtut.block.ScaffoldedRepeater;
import grill24.hoopyfroodtut.block.ScaffoldedRightAngledRepeater;
import grill24.hoopyfroodtut.block.ReleaseLatch;
import grill24.hoopyfroodtut.block.ScaffoldedReleaseLatch;
import grill24.hoopyfroodtut.block.ScaffoldedSluggishReleaseLatch;
import grill24.hoopyfroodtut.block.SluggishReleaseLatch;
import grill24.hoopyfroodtut.block.ScaffoldedSluggishPulseLatch;
import grill24.hoopyfroodtut.block.SluggishPulseLatch;
import grill24.hoopyfroodtut.block.ProximitySensor;
import grill24.hoopyfroodtut.block.RedstoneClock;
import grill24.hoopyfroodtut.block.InfiniteImprobabilityDrive;
import grill24.hoopyfroodtut.block.WobblyWater;
import grill24.hoopyfroodtut.block.SomebodyElsesProblemField;
import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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

    // A physics fluid simulation block with an animated spring-mass water surface.
    public static final DeferredBlock<WobblyWater> WOBBLY_WATER = BLOCKS.registerBlock(
            "wobbly_water",
            p -> new WobblyWater(p
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(2.0f, 6.0f)));

    // A gyroscopic armillary-sphere that banishes inserted items to a seeded distant coordinate.
    public static final DeferredBlock<BanishingBin> BANISHING_BIN = BLOCKS.registerBlock(
            "banishing_bin",
            p -> new BanishingBin(p
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(3.0f, 6.0f)));

    // Inverts the input redstone signal: outputs 15 when input is 0, outputs 0 when input is > 0.
    public static final DeferredBlock<Inverter> INVERTER = BLOCKS.registerBlock("inverter",
            p -> new Inverter(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Holds a redstone signal HIGH for a configurable duration after a rising edge on its input face.
    public static final DeferredBlock<PulseLatch> PULSE_LATCH = BLOCKS.registerBlock("pulse_latch",
            p -> new PulseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Mirrors input HIGH; output stays HIGH for a configurable duration after the input falls LOW.
    public static final DeferredBlock<ReleaseLatch> RELEASE_LATCH = BLOCKS.registerBlock("release_latch",
            p -> new ReleaseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Sluggish variant of ReleaseLatch with much longer hold durations (2s, 5s, 15s, 60s).
    public static final DeferredBlock<SluggishReleaseLatch> SLUGGISH_RELEASE_LATCH = BLOCKS.registerBlock("sluggish_release_latch",
            p -> new SluggishReleaseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Sluggish variant with much longer hold durations (2s, 5s, 15s, 60s).
    public static final DeferredBlock<SluggishPulseLatch> SLUGGISH_PULSE_LATCH = BLOCKS.registerBlock("sluggish_pulse_latch",
            p -> new SluggishPulseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Self-oscillating redstone pulse generator; input face pauses the clock.
    public static final DeferredBlock<RedstoneClock> REDSTONE_CLOCK = BLOCKS.registerBlock("redstone_clock",
            p -> new RedstoneClock(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Sluggish variant of RedstoneClock with much longer periods (4s, 10s, 30s, 2min).
    public static final DeferredBlock<SluggishRedstoneClock> SLUGGISH_REDSTONE_CLOCK = BLOCKS.registerBlock("sluggish_redstone_clock",
            p -> new SluggishRedstoneClock(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Emits a redstone signal (0-15) proportional to the proximity of the nearest target entity.
    public static final DeferredBlock<ProximitySensor> PROXIMITY_SENSOR = BLOCKS.registerBlock("proximity_sensor",
            p -> new ProximitySensor(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // A directional block that continuously dispenses items from its 9-slot inventory without needing redstone.
    public static final DeferredBlock<Ejector> EJECTOR = BLOCKS.registerBlock("ejector",
            p -> new Ejector(p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5f)));

    // A directional block that continuously drops items from its 9-slot inventory without needing redstone.
    public static final DeferredBlock<Expeller> EXPELLER = BLOCKS.registerBlock("expeller",
            p -> new Expeller(p.mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5f)));

    // A field emitter that makes the surrounding area invisible to mob AI and pathfinding.
    public static final DeferredBlock<SomebodyElsesProblemField> SOMEBODY_ELSES_PROBLEM_FIELD = BLOCKS.registerBlock(
            "somebody_elses_problem_field",
            p -> new SomebodyElsesProblemField(p
                    .noOcclusion()
                    .isSuffocating((_, _, _) -> false)
                    .isViewBlocking((_, _, _) -> false)
                    .strength(3.0f, 6.0f)
                    .lightLevel(state -> 5)));

    // ---- Scaffolded Redstone Components -----

    // Functionally identical to Inverter but with a scaffolding frame (allows blocks above).
    public static final DeferredBlock<ScaffoldedInverter> SCAFFOLDED_INVERTER = BLOCKS.registerBlock("scaffolded_inverter",
            p -> new ScaffoldedInverter(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to vanilla Repeater but with a scaffolding frame (allows blocks above).
    public static final DeferredBlock<ScaffoldedRepeater> SCAFFOLDED_REPEATER = BLOCKS.registerBlock("scaffolded_repeater",
            p -> new ScaffoldedRepeater(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to vanilla Comparator but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedComparator> SCAFFOLDED_COMPARATOR = BLOCKS.registerBlock("scaffolded_comparator",
            p -> new ScaffoldedComparator(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to PulseLatch but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedPulseLatch> SCAFFOLDED_PULSE_LATCH = BLOCKS.registerBlock("scaffolded_pulse_latch",
            p -> new ScaffoldedPulseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to SluggishPulseLatch but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedSluggishPulseLatch> SCAFFOLDED_SLUGGISH_PULSE_LATCH = BLOCKS.registerBlock("scaffolded_sluggish_pulse_latch",
            p -> new ScaffoldedSluggishPulseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to ReleaseLatch but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedReleaseLatch> SCAFFOLDED_RELEASE_LATCH = BLOCKS.registerBlock("scaffolded_release_latch",
            p -> new ScaffoldedReleaseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to SluggishReleaseLatch but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedSluggishReleaseLatch> SCAFFOLDED_SLUGGISH_RELEASE_LATCH = BLOCKS.registerBlock("scaffolded_sluggish_release_latch",
            p -> new ScaffoldedSluggishReleaseLatch(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to SluggishRedstoneClock but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedSluggishRedstoneClock> SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK = BLOCKS.registerBlock("scaffolded_sluggish_redstone_clock",
            p -> new ScaffoldedSluggishRedstoneClock(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to RedstoneClock but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedRedstoneClock> SCAFFOLDED_REDSTONE_CLOCK = BLOCKS.registerBlock("scaffolded_redstone_clock",
            p -> new ScaffoldedRedstoneClock(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Functionally identical to vanilla RedstoneWire but with a scaffolding frame.
    public static final DeferredBlock<ScaffoldedRedstoneDust> SCAFFOLDED_REDSTONE_DUST = BLOCKS.registerBlock("scaffolded_redstone_dust",
            p -> new ScaffoldedRedstoneDust(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // ---- Angled Repeaters -----

    // Reads from counter-clockwise side, outputs in FACING direction.
    public static final DeferredBlock<LeftAngledRepeater> LEFT_ANGLED_REPEATER = BLOCKS.registerBlock("left_angled_repeater",
            p -> new LeftAngledRepeater(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Reads from clockwise side, outputs in FACING direction.
    public static final DeferredBlock<RightAngledRepeater> RIGHT_ANGLED_REPEATER = BLOCKS.registerBlock("right_angled_repeater",
            p -> new RightAngledRepeater(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY)));

    // Scaffolded variant of LeftAngledRepeater.
    public static final DeferredBlock<ScaffoldedLeftAngledRepeater> SCAFFOLDED_LEFT_ANGLED_REPEATER = BLOCKS.registerBlock("scaffolded_left_angled_repeater",
            p -> new ScaffoldedLeftAngledRepeater(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // Scaffolded variant of RightAngledRepeater.
    public static final DeferredBlock<ScaffoldedRightAngledRepeater> SCAFFOLDED_RIGHT_ANGLED_REPEATER = BLOCKS.registerBlock("scaffolded_right_angled_repeater",
            p -> new ScaffoldedRightAngledRepeater(p.instabreak().sound(SoundType.STONE).noOcclusion().pushReaction(PushReaction.DESTROY).isRedstoneConductor((_, _, _) -> false)));

    // ---- Block Families -----
    public static final Lazy<BlockFamily> BROWN_BRICKS_FAMILY = Lazy.lazy(() -> new BlockFamily.Builder(HoopyFroodTutBlocks.BROWN_BRICKS.get())
            .slab(HoopyFroodTutBlocks.BROWN_BRICKS_SLAB.get())
            .stairs(HoopyFroodTutBlocks.BROWN_BRICKS_STAIRS.get())
            .generateStonecutterRecipe()
            .getFamily()
    );
}
