package grill24.hoopyfroodtut.core;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class Util {
    public static Identifier hft(String path) {
        return Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, path);
    }

    /**
     * Client-side only. Adds tooltip lines via {@code lines} when shift is held,
     * otherwise adds a single "Hold Shift for details" hint.
     */
    public static void appendShiftableTooltip(Consumer<Component> components, Runnable lines) {
        if (Minecraft.getInstance().hasShiftDown()) {
            lines.run();
        } else {
            components.accept(Component.translatable("tooltip.hoopyfroodtut.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}
