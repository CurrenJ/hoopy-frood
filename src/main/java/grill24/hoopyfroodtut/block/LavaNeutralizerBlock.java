package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.blockentity.LavaNeutralizerBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LavaNeutralizerBlock extends BaseEntityBlock {

    public static final MapCodec<LavaNeutralizerBlock> CODEC = simpleCodec(LavaNeutralizerBlock::new);
    public static final int MIN_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", MIN_CHARGES, MAX_CHARGES);

    public LavaNeutralizerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGES, MAX_CHARGES));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(CHARGES);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        stack.set(HoopyFroodDataComponents.LAVA_NEUTRALIZER_CHARGES.get(), state.getValue(CHARGES));
        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        int charges = stack.getOrDefault(HoopyFroodDataComponents.LAVA_NEUTRALIZER_CHARGES.get(), MAX_CHARGES);
        return defaultBlockState().setValue(CHARGES, Math.clamp(charges, MIN_CHARGES, MAX_CHARGES));
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LavaNeutralizerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type,
                HoopyFroodBlockEntityTypes.LAVA_NEUTRALIZER.get(),
                LavaNeutralizerBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        Item rechargeItem = getRechargeItem();
        if (rechargeItem != null && stack.is(rechargeItem)) {
            int charges = state.getValue(CHARGES);
            if (charges < MAX_CHARGES) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(CHARGES, charges + 1), 3);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Speed potion → temporary speed boost
        if (level.getBlockEntity(pos) instanceof LavaNeutralizerBlockEntity be) {
            PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            for (var effect : potionContents.getAllEffects()) {
                if (effect.is(MobEffects.SPEED)) {
                    if (!level.isClientSide()) {
                        int halfDuration = Math.max(1, effect.getDuration() / 2);
                        int multiplier = 1 << (effect.getAmplifier() + 1); // lvl 1 → 2x, lvl 2 → 4x
                        be.setSpeedBoost(halfDuration, multiplier);
                        stack.shrink(1);
                        if (!player.getAbilities().instabuild) {
                            ItemStack bottle = Items.GLASS_BOTTLE.getDefaultInstance();
                            if (!player.getInventory().add(bottle)) {
                                player.drop(bottle, false);
                            }
                        }
                        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if (menuProvider != null) {
            player.openMenu(menuProvider);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if (menuProvider != null) {
            player.openMenu(menuProvider);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    private static Item getRechargeItem() {
        String id = Config.LAVA_NEUTRALIZER_RECHARGE_ITEM.get();
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
    }
}
