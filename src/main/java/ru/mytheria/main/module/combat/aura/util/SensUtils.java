package ru.mytheria.main.module.combat.aura.util;

import net.minecraft.client.MinecraftClient;

public class SensUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean exists() {
        return true;
    }

    public static float getGCDValue() {
        double sensitivity = mc.options.getMouseSensitivity().getValue();
        return (float) (0.15 * (sensitivity * sensitivity * sensitivity) + 0.35);
    }
}