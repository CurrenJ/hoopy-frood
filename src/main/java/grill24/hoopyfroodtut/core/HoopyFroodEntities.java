package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.entity.InertPrimedTnt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, HoopyFroodTut.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<InertPrimedTnt>> INERT_PRIMED_TNT =
            ENTITY_TYPES.register("inert_primed_tnt",
                    () -> EntityType.Builder.<InertPrimedTnt>of(InertPrimedTnt::new, MobCategory.MISC)
                            .sized(0.98F, 0.98F)
                            .eyeHeight(0.15F)
                            .clientTrackingRange(10)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Util.hft("inert_primed_tnt"))));
}
