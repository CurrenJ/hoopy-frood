package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeggingItemScrabblerBlockEntity>> BEGGING_ITEM_SCRABBLER =
            BLOCK_ENTITY_TYPES.register("begging_item_scrabbler",
                    () -> new BlockEntityType<>(BeggingItemScrabblerBlockEntity::new, HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get()));
}
