package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import grill24.hoopyfroodtut.blockentity.BanishingBinBlockEntity;
import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.blockentity.EjectorBlockEntity;
import grill24.hoopyfroodtut.blockentity.PrecisionDispenserBlockEntity;
import grill24.hoopyfroodtut.blockentity.ExpellerBlockEntity;
import grill24.hoopyfroodtut.blockentity.PulseLatchBlockEntity;
import grill24.hoopyfroodtut.blockentity.ReleaseLatchBlockEntity;
import grill24.hoopyfroodtut.blockentity.ScaffoldedComparatorBlockEntity;
import grill24.hoopyfroodtut.blockentity.SluggishPulseLatchBlockEntity;
import grill24.hoopyfroodtut.blockentity.SluggishReleaseLatchBlockEntity;
import grill24.hoopyfroodtut.blockentity.ProximitySensorBlockEntity;
import grill24.hoopyfroodtut.blockentity.RedstoneClockBlockEntity;
import grill24.hoopyfroodtut.blockentity.SluggishRedstoneClockBlockEntity;
import grill24.hoopyfroodtut.blockentity.InfiniteImprobabilityDriveBlockEntity;
import grill24.hoopyfroodtut.blockentity.WobblyWaterBlockEntity;
import grill24.hoopyfroodtut.blockentity.LavaNeutralizerBlockEntity;
import grill24.hoopyfroodtut.blockentity.SomebodyElsesProblemFieldBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HoopyFroodTut.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BalancerNodeBlockEntity>> BALANCER_NODE =
            BLOCK_ENTITY_TYPES.register("balancer_node",
                    () -> new BlockEntityType<>(BalancerNodeBlockEntity::new, HoopyFroodTutBlocks.BALANCER_NODE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DisposableCaterpillarBlockEntity>> DISPOSABLE_CATERPILLAR =
            BLOCK_ENTITY_TYPES.register("disposable_caterpillar",
                    () -> new BlockEntityType<>(DisposableCaterpillarBlockEntity::new, HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfiniteImprobabilityDriveBlockEntity>> INFINITE_IMPROBABILITY_DRIVE =
            BLOCK_ENTITY_TYPES.register("infinite_improbability_drive",
                    () -> new BlockEntityType<>(InfiniteImprobabilityDriveBlockEntity::new,
                            HoopyFroodTutBlocks.INFINITE_IMPROBABILITY_DRIVE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeggingItemScrabblerBlockEntity>> BEGGING_ITEM_SCRABBLER =
            BLOCK_ENTITY_TYPES.register("begging_item_scrabbler",
                    () -> new BlockEntityType<>(BeggingItemScrabblerBlockEntity::new, HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SomebodyElsesProblemFieldBlockEntity>> SOMEBODY_ELSES_PROBLEM_FIELD =
            BLOCK_ENTITY_TYPES.register("somebody_elses_problem_field",
                    () -> new BlockEntityType<>(SomebodyElsesProblemFieldBlockEntity::new,
                            HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WobblyWaterBlockEntity>> WOBBLY_WATER =
            BLOCK_ENTITY_TYPES.register("wobbly_water",
                    () -> new BlockEntityType<>(WobblyWaterBlockEntity::new,
                            HoopyFroodTutBlocks.WOBBLY_WATER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PulseLatchBlockEntity>> PULSE_LATCH =
            BLOCK_ENTITY_TYPES.register("pulse_latch",
                    () -> new BlockEntityType<>(PulseLatchBlockEntity::new,
                            HoopyFroodTutBlocks.PULSE_LATCH.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReleaseLatchBlockEntity>> RELEASE_LATCH =
            BLOCK_ENTITY_TYPES.register("release_latch",
                    () -> new BlockEntityType<>(ReleaseLatchBlockEntity::new,
                            HoopyFroodTutBlocks.RELEASE_LATCH.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluggishReleaseLatchBlockEntity>> SLUGGISH_RELEASE_LATCH =
            BLOCK_ENTITY_TYPES.register("sluggish_release_latch",
                    () -> new BlockEntityType<>(SluggishReleaseLatchBlockEntity::new,
                            HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluggishPulseLatchBlockEntity>> SLUGGISH_PULSE_LATCH =
            BLOCK_ENTITY_TYPES.register("sluggish_pulse_latch",
                    () -> new BlockEntityType<>(SluggishPulseLatchBlockEntity::new,
                            HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneClockBlockEntity>> REDSTONE_CLOCK =
            BLOCK_ENTITY_TYPES.register("redstone_clock",
                    () -> new BlockEntityType<>(RedstoneClockBlockEntity::new,
                            HoopyFroodTutBlocks.REDSTONE_CLOCK.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SluggishRedstoneClockBlockEntity>> SLUGGISH_REDSTONE_CLOCK =
            BLOCK_ENTITY_TYPES.register("sluggish_redstone_clock",
                    () -> new BlockEntityType<>(SluggishRedstoneClockBlockEntity::new,
                            HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get(),
                            HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScaffoldedComparatorBlockEntity>> SCAFFOLDED_COMPARATOR =
            BLOCK_ENTITY_TYPES.register("scaffolded_comparator",
                    () -> new BlockEntityType<>(ScaffoldedComparatorBlockEntity::new,
                            HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ProximitySensorBlockEntity>> PROXIMITY_SENSOR =
            BLOCK_ENTITY_TYPES.register("proximity_sensor",
                    () -> new BlockEntityType<>(ProximitySensorBlockEntity::new, HoopyFroodTutBlocks.PROXIMITY_SENSOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrecisionDispenserBlockEntity>> PRECISION_DISPENSER =
            BLOCK_ENTITY_TYPES.register("precision_dispenser",
                    () -> new BlockEntityType<>(PrecisionDispenserBlockEntity::new, HoopyFroodTutBlocks.PRECISION_DISPENSER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EjectorBlockEntity>> EJECTOR =
            BLOCK_ENTITY_TYPES.register("ejector",
                    () -> new BlockEntityType<>(EjectorBlockEntity::new, HoopyFroodTutBlocks.EJECTOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExpellerBlockEntity>> EXPELLER =
            BLOCK_ENTITY_TYPES.register("expeller",
                    () -> new BlockEntityType<>(ExpellerBlockEntity::new, HoopyFroodTutBlocks.EXPELLER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LavaNeutralizerBlockEntity>> LAVA_NEUTRALIZER =
            BLOCK_ENTITY_TYPES.register("lava_neutralizer",
                    () -> new BlockEntityType<>(LavaNeutralizerBlockEntity::new,
                            HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BanishingBinBlockEntity>> BANISHING_BIN =
            BLOCK_ENTITY_TYPES.register("banishing_bin",
                    () -> new BlockEntityType<>(BanishingBinBlockEntity::new,
                            HoopyFroodTutBlocks.BANISHING_BIN.get()));
}
