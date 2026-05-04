package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class RoundTripMagicMirrorItem extends Item {
    private static final int USE_DURATION = 40; // 2 seconds

    public RoundTripMagicMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.ROUND_TRIP_MAGIC_MIRROR_MAX_DURABILITY.get();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            Vec3 destination = getDestination(serverPlayer, player.getItemInHand(hand));
            double cost = MagicMirrorItem.computeCost(player.position(), destination);
            double available = player.experienceLevel + player.experienceProgress;
            if (player.isCreative() || available >= cost) {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.round_trip_magic_mirror.charging", MagicMirrorItem.fmt(cost))
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.magic_mirror.insufficient_xp", MagicMirrorItem.fmt(cost))
                        .withStyle(ChatFormatting.RED));
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            Vec3 origin = stack.get(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get());
            boolean returning = origin != null;

            Vec3 destination = returning ? origin
                : player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING).position();
            double cost = MagicMirrorItem.computeCost(player.position(), destination);
            double available = player.experienceLevel + player.experienceProgress;

            if (player.isCreative() || available >= cost) {
                if (!player.isCreative()) {
                    MagicMirrorItem.deductFractionalLevels(player, cost);
                }

                Vec3 fromPos = player.position();
                ServerLevel fromLevel = (ServerLevel) player.level();
                spawnTeleportParticles(fromLevel, fromPos);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

                TeleportTransition transition;
                if (returning) {
                    transition = new TeleportTransition(
                        (ServerLevel) player.level(), origin, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING);
                    stack.remove(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get());
                } else {
                    transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
                    stack.set(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get(), fromPos);
                }

                player.teleport(transition);
                spawnTeleportParticles(transition.newLevel(), transition.position());
                transition.newLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

                if (!player.isCreative()) {
                    EquipmentSlot slot = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND
                        : EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
            } else {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.magic_mirror.insufficient_xp", MagicMirrorItem.fmt(cost))
                        .withStyle(ChatFormatting.RED));
            }
        }
        return stack;
    }

    private static Vec3 getDestination(ServerPlayer player, ItemStack stack) {
        Vec3 origin = stack.get(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get());
        if (origin != null) {
            return origin;
        }
        TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
        return transition.position();
    }

    public static boolean isReturning(ItemStack stack) {
        return stack.has(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get());
    }

    private static void spawnTeleportParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,  pos.x, pos.y + 1.0, pos.z, 80, 0.3, 0.9, 0.3, 0.5);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 30, 0.5, 0.5, 0.5, 0.15);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            if (isReturning(stack)) {
                Vec3 origin = stack.get(HoopyFroodDataComponents.ROUND_TRIP_ORIGIN.get());
                String coords = origin != null
                    ? String.format("%d, %d, %d", (int) origin.x, (int) origin.y, (int) origin.z)
                    : "unknown";
                components.accept(
                    Component.literal("Hold ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("right-click").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" to charge, then release to return to ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(coords).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(".").withStyle(ChatFormatting.GRAY))
                );
            } else {
                components.accept(
                    Component.literal("Hold ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("right-click").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" to charge, then release to teleport to your ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("spawn point").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(".").withStyle(ChatFormatting.GRAY))
                );
                int blocksPerLevel = Config.MAGIC_MIRROR_BLOCKS_PER_LEVEL.get();
                components.accept(
                    Component.literal("Costs ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("1.0 levels").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" per ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(blocksPerLevel + " blocks").withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" of travel.").withStyle(ChatFormatting.GRAY))
                );
            }
            components.accept(
                Component.literal("Use again to return — then the mirror shatters.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
            );
        });
    }
}
