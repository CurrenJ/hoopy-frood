package grill24.hoopyfroodtut.core;

import net.minecraft.resources.Identifier;

public class Util {
    public static Identifier hft(String path) {
        return Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, path);
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }
}
