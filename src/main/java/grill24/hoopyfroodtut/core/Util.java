package grill24.hoopyfroodtut.core;

import net.minecraft.resources.Identifier;

public class Util {
    public static Identifier hft(String path) {
        return Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, path);
    }
}
