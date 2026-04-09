package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import grill24.hoopyfroodtut.blockentity.BanishingBinBlockEntity;
import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.blockentity.InfiniteImprobabilityDriveBlockEntity;
import grill24.hoopyfroodtut.blockentity.WobblyWaterBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BanishingBinBlockEntity>> BANISHING_BIN =
            BLOCK_ENTITY_TYPES.register("banishing_bin",
                    () -> new BlockEntityType<>(BanishingBinBlockEntity::new,
                            HoopyFroodTutBlocks.BANISHING_BIN.get()));
}
