package grill24.hoopyfroodtut.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
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

    /**
     * Number of metallic nuggets (fuel) pre-loaded into a Begging Item Scrabbler item.
     * Each nugget grants one block of movement. Defaults to 0 when absent.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCRABBLER_NUGGETS =
            DATA_COMPONENT_TYPES.register("scrabbler_nuggets",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    /**
     * Home container block position pre-linked to a Begging Item Scrabbler item.
     * The scrabbler returns here to deposit collected items. Absent when no home is set.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SCRABBLER_HOME =
            DATA_COMPONENT_TYPES.register("scrabbler_home",
                    () -> DataComponentType.<BlockPos>builder()
                            .persistent(BlockPos.CODEC)
                            .networkSynchronized(BlockPos.STREAM_CODEC)
                            .build());

    /**
     * Whether a Disposable Caterpillar is "stuck" (immobile). When true, each triggered
     * mining cycle mines the block directly ahead and decrements charges, but the caterpillar
     * never advances forward. Useful for static farming setups.
     * Defaults to false (absent). Set by crafting with a cobweb.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CATERPILLAR_IMMOBILE =
            DATA_COMPONENT_TYPES.register("caterpillar_immobile",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    /**
     * Whether a Disposable Caterpillar will continue to exist after its charges are depleted.
     * When true, the caterpillar will mine and consume charges as normal, but won't destroy itself.
     * Defaults to false (absent). Set by crafting with a Nether Star.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CATERPILLAR_UNDYING =
            DATA_COMPONENT_TYPES.register("caterpillar_undying",
                    () -> DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());

    /**
     * Number of inventory slots on a Begging Item Scrabbler item.
     * Defaults to {@code BeggingItemScrabblerBlockEntity.DEFAULT_INVENTORY_SIZE} when absent.
     * Increased by crafting with a chest or right-clicking the placed block with a chest.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCRABBLER_SLOTS =
            DATA_COMPONENT_TYPES.register("scrabbler_slots",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    /**
     * Cached teleport destination for the Magic Mirror. Stored server-side while held and
     * network-synced so the client tooltip can show live cost and target coordinates.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Vec3>> MAGIC_MIRROR_DESTINATION =
            DATA_COMPONENT_TYPES.register("magic_mirror_destination",
                    () -> DataComponentType.<Vec3>builder()
                            .persistent(Vec3.CODEC)
                            .networkSynchronized(Vec3.STREAM_CODEC)
                            .build());
}
