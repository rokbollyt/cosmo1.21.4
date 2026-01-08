package fun.cosmo.main.module.combat.aura;

import net.minecraft.client.network.ClientPlayerEntity;

public class LocalRotationManager {
    private static float originalYaw = 0;
    private static float originalPitch = 0;
    private static boolean isOverriding = false;

    public static void saveOriginalRotation(ClientPlayerEntity player) {
        if (!isOverriding) {
            originalYaw = player.getYaw();
            originalPitch = player.getPitch();
            isOverriding = true;
        }
    }

    public static void restoreOriginalRotation(ClientPlayerEntity player) {
        if (isOverriding) {
            player.setYaw(originalYaw);
            player.setPitch(originalPitch);
            isOverriding = false;
        }
    }

    public static boolean isOverriding() {
        return isOverriding;
    }
}