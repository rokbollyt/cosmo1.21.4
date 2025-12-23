package ru.mytheria.api.util.player.move;

import ru.mytheria.api.clientannotation.QuickImport;

public class MovingUtil implements QuickImport {
    public static void setMotion(final double speed) {
        if (!isMoving()) return;

        final double yaw = direction();
        mc.player.setVelocity(-Math.sin(yaw) * speed, mc.player.getVelocity().y, Math.cos(yaw) * speed);
    }

    public static double direction() {
        float pYRot = (float) mc.player.getY();

        float forward = mc.player.input.movementForward;
        float left = mc.player.input.movementSideways;

        if (forward < 0) {
            pYRot += 180;
        }

        float pForward = 1.0F;

        if (forward < 0) {
            pForward = -0.5F;
        } else if (forward > 0) {
            pForward = 0.5F;
        }

        if (left > 0) {
            pYRot -= 90 * pForward;
        } else if (left < 0) {
            pYRot += 90 * pForward;
        }

        return Math.toRadians(pYRot);
    }

    public static boolean isMoving() {
        return mc.player.input.movementForward != 0f || mc.player.input.movementSideways != 0f;
    }
}
