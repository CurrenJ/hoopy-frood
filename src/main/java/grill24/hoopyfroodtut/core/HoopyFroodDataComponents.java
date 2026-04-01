package grill24.hoopyfroodtut.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, HoopyFroodTut.MODID);

    /**
     * Number of blocks a Disposable Caterpillar item can mine before breaking.
     * Defaults to 1 when absent. Can be stacked via the caterpillar_combine crafting recipe.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CATERPILLAR_CHARGES =
            DATA_COMPONENT_TYPES.register("caterpillar_charges",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    /**
     * Number of torches a Disposable Caterpillar item carries. One torch is consumed
     * every 12 blocks traveled and placed at the caterpillar's previous position.
     * Defaults to 0 (no torches) when absent.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CATERPILLAR_TORCHES =
            DATA_COMPONENT_TYPES.register("caterpillar_torches",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());
}
