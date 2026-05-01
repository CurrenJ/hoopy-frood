package grill24.hoopyfroodtut.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles in-world conversion of redstone components to their scaffolded variants
 * by right-clicking with Scaffolding. Registered on NeoForge.EVENT_BUS in HoopyFroodTut.
 */
public class ScaffoldingConversionEvents {

    /**
     * Maps each base block to its scaffolded variant.
     * Populated lazily on first use (after blocks are registered).
     */
    private static @Nullable Map<Block, Block> scaffoldMap = null;

    public static Map<Block, Block> getScaffoldMap() {
        if (scaffoldMap == null) {
            scaffoldMap = new HashMap<>();
            scaffoldMap.put(net.minecraft.world.level.block.Blocks.REPEATER,
                    HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get());
            scaffoldMap.put(net.minecraft.world.level.block.Blocks.COMPARATOR,
                    HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get());
            scaffoldMap.put(HoopyFroodTutBlocks.PULSE_LATCH.get(),
                    HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get());
            scaffoldMap.put(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get(),
                    HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get());
            scaffoldMap.put(HoopyFroodTutBlocks.REDSTONE_CLOCK.get(),
                    HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get());
            scaffoldMap.put(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE,
                    HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get());
            scaffoldMap.put(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get(),
                    HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get());
            scaffoldMap.put(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get(),
                    HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get());
        }
        return scaffoldMap;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        if (!(held.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ScaffoldingBlock)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block scaffoldedBlock = getScaffoldMap().get(state.getBlock());
        if (scaffoldedBlock == null) return;

        // Capture block entity data before changing the block
        @Nullable CompoundTag savedData = null;
        BlockEntity oldBe = level.getBlockEntity(pos);
        if (oldBe != null) {
            savedData = oldBe.saveWithoutMetadata(level.registryAccess());
        }

        // Build the new block state, copying all common properties
        BlockState newState = scaffoldedBlock.defaultBlockState();
        for (Property<?> prop : newState.getProperties()) {
            if (state.hasProperty(prop)) {
                newState = copyProperty(state, newState, prop);
            }
        }

        // Set the new block (creates new BE if the scaffolded block has one)
        level.setBlock(pos, newState, Block.UPDATE_ALL);

        // Restore BE data into the newly created block entity
        if (savedData != null) {
            BlockEntity newBe = level.getBlockEntity(pos);
            if (newBe != null) {
                HolderLookup.Provider registries = level.registryAccess();
                newBe.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, savedData));
                newBe.setChanged();
            }
        }

        if (!player.isCreative()) held.shrink(1);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
    }

    /**
     * Copies a single property value from the source state to the target state.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState source, BlockState target, Property<T> property) {
        return target.setValue(property, source.getValue(property));
    }
}
