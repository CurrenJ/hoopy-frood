package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.block.LavaNeutralizerBlock;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class LavaNeutralizerBlockEntity extends BaseContainerBlockEntity {

    private static final int CONTAINER_SIZE = 27;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    private int speedBoostTicks;
    private int speedMultiplier = 1;

    public LavaNeutralizerBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.LAVA_NEUTRALIZER.get(), pos, state);
    }

    public void setSpeedBoost(int durationTicks, int multiplier) {
        this.speedBoostTicks = durationTicks;
        this.speedMultiplier = multiplier;
        setChanged();
    }

    public boolean hasSpeedBoost() {
        return speedBoostTicks > 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        speedBoostTicks = input.getIntOr("speed_boost_ticks", 0);
        speedMultiplier = input.getIntOr("speed_multiplier", 1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("speed_boost_ticks", speedBoostTicks);
        output.putInt("speed_multiplier", speedMultiplier);
    }

    // ── Container ──────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.hoopyfroodtut.lava_neutralizer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(HoopyFroodDataComponents.LAVA_NEUTRALIZER_CHARGES.get(),
                getBlockState().getValue(LavaNeutralizerBlock.CHARGES));
    }

    // ── Server tick ────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, LavaNeutralizerBlockEntity be) {
        // Count down speed boost
        if (be.speedBoostTicks > 0) {
            be.speedBoostTicks--;
            if (be.speedBoostTicks == 0) {
                be.speedMultiplier = 1;
                be.setChanged();
            }
        }

        if (level.hasNeighborSignal(pos)) {
            return;
        }

        int charges = state.getValue(LavaNeutralizerBlock.CHARGES);
        if (charges <= 0) {
            return;
        }

        int baseTickRate = Config.LAVA_NEUTRALIZER_TICK_RATE.get();
        int effectiveTickRate = Math.max(1, baseTickRate / be.speedMultiplier);
        if (baseTickRate <= 0 || level.getGameTime() % effectiveTickRate != 0) {
            return;
        }

        int radius = Config.LAVA_NEUTRALIZER_RADIUS.get();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    checkPos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);

                    if (level.getFluidState(checkPos).is(Fluids.LAVA) && level.getFluidState(checkPos).isSource()) {
                        int slot = findFirstFilledBlockSlot(be);
                        if (slot < 0) {
                            return;
                        }

                        ItemStack stack = be.items.get(slot);
                        BlockItem blockItem = (BlockItem) stack.getItem();
                        BlockState fillState = blockItem.getBlock().defaultBlockState();

                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            be.items.set(slot, ItemStack.EMPTY);
                        }

                        level.setBlock(checkPos, fillState, 3);
                        level.playSound(null, checkPos, fillState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

                        double depleteChance = Config.LAVA_NEUTRALIZER_CHARGE_DEPLETE_CHANCE.get();
                        if (level.getRandom().nextDouble() < depleteChance) {
                            level.setBlock(pos, state.setValue(LavaNeutralizerBlock.CHARGES, charges - 1), 3);
                        }

                        be.setChanged();
                        return;
                    }
                }
            }
        }
    }

    private static int findFirstFilledBlockSlot(LavaNeutralizerBlockEntity be) {
        for (int i = 0; i < be.items.size(); i++) {
            ItemStack stack = be.items.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
