package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class MagicMirrorItem extends Item {
    private static final int USE_DURATION = 40; // 2 seconds

    public MagicMirrorItem(Properties properties) {
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            TeleportTransition transition = serverPlayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            double cost = computeCost(player.position(), transition.position());
            double available = player.experienceLevel + player.experienceProgress;
            if (player.isCreative() || available >= cost) {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.magic_mirror.charging", fmt(cost))
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.magic_mirror.insufficient_xp", fmt(cost))
                        .withStyle(ChatFormatting.RED));
            }
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            double cost = computeCost(player.position(), transition.position());
            double available = player.experienceLevel + player.experienceProgress;
            if (player.isCreative() || available >= cost) {
                if (!player.isCreative()) {
                    deductFractionalLevels(player, cost);
                }
                ServerLevel fromLevel = (ServerLevel) player.level();
                Vec3 fromPos = player.position();
                spawnTeleportParticles(fromLevel, fromPos);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.teleport(transition);
                spawnTeleportParticles(transition.newLevel(), transition.position());
                transition.newLevel().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                player.sendOverlayMessage(
                    Component.translatable("item.hoopyfroodtut.magic_mirror.insufficient_xp", fmt(cost))
                        .withStyle(ChatFormatting.RED));
            }
        }
        return stack;
    }

    private static void deductFractionalLevels(ServerPlayer player, double cost) {
        double newTotal = Math.max(0.0, player.experienceLevel + player.experienceProgress - cost);
        int newLevel = (int) newTotal;
        float newProgress = (float) (newTotal - newLevel);
        // Write progress first, then setExperienceLevels — that method marks lastSentExp dirty,
        // which causes the next tick to flush both fields in the same ClientboundSetExperiencePacket.
        player.experienceProgress = newProgress;
        player.setExperienceLevels(newLevel);
    }

    private static void spawnTeleportParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,  pos.x, pos.y + 1.0, pos.z, 80, 0.3, 0.9, 0.3, 0.5);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 30, 0.5, 0.5, 0.5, 0.15);
    }

    private static double computeCost(Vec3 from, Vec3 to) {
        double distance = from.distanceTo(to);
        if (distance < 1.0) return 0.0;
        return distance / Config.MAGIC_MIRROR_BLOCKS_PER_LEVEL.get();
    }

    private static String fmt(double cost) {
        return String.format("%.1f", cost);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            components.accept(
                Component.literal("Hold ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("right-click").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" to charge, then release to teleport to your ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("spawn point").withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(".").withStyle(ChatFormatting.GRAY))
            );

            Vec3 dest = stack.get(HoopyFroodDataComponents.MAGIC_MIRROR_DESTINATION.get());
            if (dest != null) {
                net.minecraft.world.entity.player.Player clientPlayer = Minecraft.getInstance().player;
                if (clientPlayer != null) {
                    double cost = computeCost(clientPlayer.position(), dest);
                    String coords = String.format("%d, %d, %d", (int) dest.x, (int) dest.y, (int) dest.z);
                    components.accept(
                        Component.literal("Currently: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(fmt(cost) + " levels").withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(coords).withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(")").withStyle(ChatFormatting.GRAY))
                    );
                }
            }

            int blocksPerLevel = Config.MAGIC_MIRROR_BLOCKS_PER_LEVEL.get();
            components.accept(
                Component.literal("Costs ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("1.0 levels").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" per ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(blocksPerLevel + " blocks").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" of travel.").withStyle(ChatFormatting.GRAY))
            );
        });
    }
}
