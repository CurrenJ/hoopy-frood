package grill24.hoopyfroodtut.item;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class DeathRecallMirrorItem extends Item {
    public DeathRecallMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 destination = stack.get(HoopyFroodDataComponents.DEATH_RECALL_LOCATION.get());
        if (destination == null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                    Component.translatable("item.hoopyfroodtut.death_recall_mirror.unlinked")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerLevel fromLevel = (ServerLevel) serverPlayer.level();
            Vec3 fromPos = serverPlayer.position();
            spawnTeleportParticles(fromLevel, fromPos);
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

            ServerLevel destLevel = fromLevel; // same dimension
            TeleportTransition transition = new TeleportTransition(
                destLevel, destination, Vec3.ZERO, serverPlayer.getYRot(), serverPlayer.getXRot(),
                TeleportTransition.DO_NOTHING);
            serverPlayer.teleport(transition);

            spawnTeleportParticles(destLevel, destination);
            destLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void spawnTeleportParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,  pos.x, pos.y + 1.0, pos.z, 80, 0.3, 0.9, 0.3, 0.5);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 30, 0.5, 0.5, 0.5, 0.15);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, components, flag);
        Util.appendShiftableTooltip(components, () -> {
            Vec3 deathPos = stack.get(HoopyFroodDataComponents.DEATH_RECALL_LOCATION.get());
            if (deathPos != null) {
                String coords = String.format("%d, %d, %d", (int) deathPos.x, (int) deathPos.y, (int) deathPos.z);
                components.accept(
                    Component.literal("Right-click").withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(" to return to your death at ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(coords).withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(".").withStyle(ChatFormatting.GRAY))
                );
                components.accept(
                    Component.literal("The mirror will shatter on use.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                );
            } else {
                components.accept(
                    Component.literal("Unlinked — carry it when you die").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" to bind it to your death location.").withStyle(ChatFormatting.GRAY))
                );
            }
        });
    }
}
