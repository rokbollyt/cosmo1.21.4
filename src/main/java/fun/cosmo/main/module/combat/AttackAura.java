package fun.cosmo.main.module.combat;

import fun.cosmo.api.events.impl.EventTick;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.SliderSetting;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.angle.AngleHandler;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import fun.cosmo.main.module.combat.aura.util.TargetEntitySelector;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttackAura extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final TargetEntitySelector selector = new TargetEntitySelector();
    private final Random random = new Random();

    private Angle currentServerAngle = null;
    private float lastYaw, lastPitch;
    private double sideNoise, heightNoise;
    private LivingEntity lastTarget = null;

    private final SliderSetting range = new SliderSetting(Text.of("Дистанция"), Text.of(""), () -> true)
            .set(1.0f, 6.0f, 0.1f).set(3.8f);

    private final List<Setting> settings = new ArrayList<>();

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
        settings.add(range);
    }

    @Override
    public void activate() {
        super.activate();
        currentServerAngle = null;
        lastTarget = null;
        if (mc.player != null) {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }

    // В deactivate() AttackAura.java добавь:
    @Override
    public void deactivate() {
        selector.releaseTarget();
        currentServerAngle = null;
        lastTarget = null;
        RotationController.INSTANCE.setServerAngle(null);

        if (mc.player != null) {
            mc.player.setYaw(lastYaw);
            mc.player.setPitch(lastPitch);
        }

        super.deactivate();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = selector.updateTarget(
                List.of(TargetEntitySelector.TargetType.UNARMORED_PLAYERS,
                        TargetEntitySelector.TargetType.ARMORED_PLAYERS),
                mc.world.getEntities(),
                range.getValue()
        );

        if (target == null) {
            if (currentServerAngle != null) {
                resetRotation();
            }
            lastTarget = null;
            return;
        }

        if (lastTarget != target) {
            sideNoise = 0;
            heightNoise = 0;
            lastTarget = target;
        }

        if (currentServerAngle == null) {
            currentServerAngle = new Angle(mc.player.getYaw(), mc.player.getPitch());
        }

        if (mc.player.age % 4 == 0) {
            sideNoise = (random.nextDouble() - 0.5) * 0.12;
            heightNoise = (random.nextDouble() - 0.5) * 0.16;
        }

        double pivot = target.getHeight() * 0.52 + heightNoise;
        Vec3d targetVec = target.getPos().add(sideNoise, pivot, sideNoise);

        double diffX = targetVec.x - mc.player.getX();
        double diffY = targetVec.y - mc.player.getEyeY();
        double diffZ = targetVec.z - mc.player.getZ();
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(-diffX, diffZ)));
        float targetPitch = (float) MathHelper.clamp(Math.toDegrees(-Math.atan2(diffY, dist)), -90, 90);

        applyCalculatedRotation(targetYaw, targetPitch, 65f + random.nextFloat() * 15f, false);
    }

    private void resetRotation() {
        if (currentServerAngle == null) return; // Добавь эту проверку

        float pYaw = mc.player.getYaw();
        float pPitch = mc.player.getPitch();

        float yawDelta = Math.abs(AngleHandler.getDelta(currentServerAngle.getYaw(), pYaw));
        float pitchDelta = Math.abs(currentServerAngle.getPitch() - pPitch);

        if (yawDelta < 1.0f && pitchDelta < 1.0f) {
            currentServerAngle = null;
            RotationController.INSTANCE.setServerAngle(null);
            lastYaw = pYaw;
            lastPitch = pPitch;
            return;
        }

        applyCalculatedRotation(pYaw, pPitch, 45f, true); // Увеличил скорость возврата
    }

    private void applyCalculatedRotation(float tYaw, float tPitch, float speed, boolean isReset) {
        float yawDelta = AngleHandler.getDelta(currentServerAngle.getYaw(), tYaw);
        float pitchDelta = tPitch - currentServerAngle.getPitch();

        double sensitivity = mc.options.getMouseSensitivity().getValue();
        double f = sensitivity * 0.6D + 0.2D;
        double gcd = f * f * f * 1.2D;

        float yawStep = MathHelper.clamp(yawDelta, -speed, speed);
        float pitchStep = MathHelper.clamp(pitchDelta, -speed, speed);

        float nextYaw = currentServerAngle.getYaw() + yawStep;
        float nextPitch = currentServerAngle.getPitch() + pitchStep;

        float finalYaw = lastYaw + (float) (Math.round((nextYaw - lastYaw) / gcd) * gcd);
        float finalPitch = lastPitch + (float) (Math.round((nextPitch - lastPitch) / gcd) * gcd);

        if (!isReset) {
            float jitterX = (random.nextFloat() * 0.024f) - 0.012f;
            float jitterY = (random.nextFloat() * 0.024f) - 0.012f;

            finalYaw += jitterX;
            finalPitch += jitterY;

            if (finalYaw == lastYaw) {
                finalYaw += (random.nextBoolean() ? 0.0215f : -0.0215f);
            }
            if (finalPitch == lastPitch) {
                finalPitch += (random.nextBoolean() ? 0.0215f : -0.0215f);
            }
        }

        finalPitch = MathHelper.clamp(finalPitch, -90f, 90f);

        currentServerAngle = new Angle(finalYaw, finalPitch);
        RotationController.INSTANCE.setServerAngle(currentServerAngle);

        lastYaw = finalYaw;
        lastPitch = finalPitch;
    }
}