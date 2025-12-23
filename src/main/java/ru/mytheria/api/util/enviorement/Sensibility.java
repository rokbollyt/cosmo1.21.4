package ru.mytheria.api.util.enviorement;

import net.minecraft.client.MinecraftClient;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class Sensibility {

    public static float getGCDValue() {
        if (mc.options == null) return 0.0F;

        float sensitivity = mc.options.getMouseSensitivity().getValue().floatValue(); // привели к float
        float f = sensitivity * 0.6F + 0.2F;
        return f * f * f * 8.0F;
    }

    public static float applyGCD(float angle) {
        float gcd = getGCDValue();
        return Math.round(angle / gcd) * gcd;
    }
}
