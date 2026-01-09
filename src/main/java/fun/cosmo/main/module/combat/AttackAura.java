package fun.cosmo.main.module.combat;

import fun.cosmo.api.events.impl.EventTick;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.SliderSetting;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.angle.AngleHandler;
import fun.cosmo.main.module.combat.aura.angle.implementation.FuntimeAngle;
import fun.cosmo.main.module.combat.aura.points.SmartPointHandler;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import fun.cosmo.main.module.combat.aura.util.TargetEntitySelector;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AttackAura extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final TargetEntitySelector selector = new TargetEntitySelector();
    private final SmartPointHandler smartPoints = new SmartPointHandler();
    private final Random random = new Random();

    private Angle currentServerAngle = null;
    private float lastYaw, lastPitch;
    private LivingEntity lastTarget = null;

    private final SliderSetting range = new SliderSetting(Text.of("Дистанция"), Text.of("Максимальная дистанция прицела"), () -> true)
            .set(1.0f, 6.0f, 0.1f)
            .set(4.2f);

    private final List<Setting> settings = new ArrayList<>();

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
        settings.add(range);

        // Funtime ротация
        RotationController.INSTANCE = new RotationController(new FuntimeAngle(), true, true, true);
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

    @Override
    public void deactivate() {
        selector.releaseTarget();
        currentServerAngle = null;
        lastTarget = null;
        RotationController.INSTANCE.setServerAngle(null);
        if (mc.player != null) {
            mc.player.setYaw(mc.player.getYaw());
            mc.player.setPitch(mc.player.getPitch());
        }
        super.deactivate();
        RotationController.INSTANCE.setServerAngle(null);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        // Не обновляем ротацию, если игрок копает (против RotationBreak)
        if (mc.interactionManager.isBreakingBlock()) {
            return;
        }

        // Получаем все LivingEntity в радиусе
        Box searchBox = mc.player.getBoundingBox().expand(range.getValue() + 2.0);
        List<LivingEntity> livingEntities = mc.world.getEntitiesByClass(LivingEntity.class, searchBox, e -> true);

        // Явное приведение для совместимости с Iterable<Entity>
        LivingEntity target = selector.updateTarget(
                List.of(TargetEntitySelector.TargetType.UNARMORED_PLAYERS, TargetEntitySelector.TargetType.ARMORED_PLAYERS),
                (Iterable<Entity>) (List<?>) livingEntities,
                range.getValue()
        );

        if (target == null) {
            // Жёстко сбрасываем всё, если таргета нет
            if (currentServerAngle != null) {
                currentServerAngle = null;
                RotationController.INSTANCE.setServerAngle(null);
                lastYaw = mc.player.getYaw();
                lastPitch = mc.player.getPitch();
            }
            lastTarget = null;
            return;
        }

        if (currentServerAngle == null) {
            currentServerAngle = new Angle(mc.player.getYaw(), mc.player.getPitch());
        }

        if (lastTarget != target) {
            lastTarget = target;
        }

        // Для киллауры интерполяция не нужна — используем 1.0f
        float tickDelta = 1.0f;

        Vec3d bestPoint = smartPoints.getBestPoint(target, tickDelta);

        double diffX = bestPoint.x - mc.player.getX();
        double diffY = bestPoint.y - mc.player.getEyeY();
        double diffZ = bestPoint.z - mc.player.getZ();

        double horizontalDist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90);
        float targetPitch = (float) MathHelper.clamp(Math.toDegrees(-Math.atan2(diffY, horizontalDist)), -90, 90);

        // Добавляем рандом скорости поворота (против AimDuplicateLook и AimModulo360)
        float rotationSpeed = 40f + random.nextFloat() * 80f; // 40-120 градусов/тик с большим разбросом

        Angle delta = AngleHandler.calculateDelta(currentServerAngle, new Angle(targetYaw, targetPitch));
        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();

        float clampedYaw = MathHelper.clamp(yawDelta, -rotationSpeed, rotationSpeed);
        float clampedPitch = MathHelper.clamp(pitchDelta, -rotationSpeed, rotationSpeed);

        // Улучшенный Funtime lerp с большим рандомом
        float lerpFactor = 0.6f + random.nextFloat() * 0.8f; // 0.6-1.4

        float newYaw = (float) MathHelper.lerp(lerpFactor, currentServerAngle.getYaw(), currentServerAngle.getYaw() + clampedYaw);
        float newPitch = (float) MathHelper.lerp(lerpFactor, currentServerAngle.getPitch(), currentServerAngle.getPitch() + clampedPitch);

        Angle smoothed = new Angle(newYaw, MathHelper.clamp(newPitch, -90f, 90f));

        // GCD с jitter (шум против дубликатов и modulo)
        double gcd = fun.cosmo.api.util.math.Math.computeGcd();
        float jitterYaw = (random.nextFloat() - 0.5f) * 0.04f; // увеличил jitter
        float jitterPitch = (random.nextFloat() - 0.5f) * 0.04f;

        float finalYaw = lastYaw + (float) (Math.round((smoothed.getYaw() + jitterYaw - lastYaw) / gcd) * gcd);
        float finalPitch = MathHelper.clamp(
                lastPitch + (float) (Math.round((smoothed.getPitch() + jitterPitch - lastPitch) / gcd) * gcd),
                -90f, 90f
        );

        currentServerAngle = new Angle(finalYaw, finalPitch);
        RotationController.INSTANCE.setServerAngle(currentServerAngle);

        lastYaw = finalYaw;
        lastPitch = finalPitch;
    }

    private void resetRotation() {
        if (currentServerAngle == null) return;

        float pYaw = mc.player.getYaw();
        float pPitch = mc.player.getPitch();

        float yawDiff = Math.abs(AngleHandler.getDelta(currentServerAngle.getYaw(), pYaw));
        float pitchDiff = Math.abs(currentServerAngle.getPitch() - pPitch);

        if (yawDiff < 2.0f && pitchDiff < 2.0f) {
            currentServerAngle = null;
            RotationController.INSTANCE.setServerAngle(null);
            lastYaw = pYaw;
            lastPitch = pPitch;
            return;
        }

        // Плавный возврат с рандомом
        float resetSpeed = 30f + random.nextFloat() * 40f;
        Angle delta = AngleHandler.calculateDelta(currentServerAngle, new Angle(pYaw, pPitch));
        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();

        float clampedYaw = MathHelper.clamp(yawDelta, -resetSpeed, resetSpeed);
        float clampedPitch = MathHelper.clamp(pitchDelta, -resetSpeed, resetSpeed);

        float lerpFactor = 0.6f + random.nextFloat() * 0.8f;

        float newYaw = (float) MathHelper.lerp(lerpFactor, currentServerAngle.getYaw(), currentServerAngle.getYaw() + clampedYaw);
        float newPitch = (float) MathHelper.lerp(lerpFactor, currentServerAngle.getPitch(), currentServerAngle.getPitch() + clampedPitch);

        Angle smoothed = new Angle(newYaw, MathHelper.clamp(newPitch, -90f, 90f));

        double gcd = fun.cosmo.api.util.math.Math.computeGcd();
        float jitterYaw = (random.nextFloat() - 0.5f) * 0.04f;
        float jitterPitch = (random.nextFloat() - 0.5f) * 0.04f;

        float finalYaw = lastYaw + (float) (Math.round((smoothed.getYaw() + jitterYaw - lastYaw) / gcd) * gcd);
        float finalPitch = MathHelper.clamp(
                lastPitch + (float) (Math.round((smoothed.getPitch() + jitterPitch - lastPitch) / gcd) * gcd),
                -90f, 90f
        );

        currentServerAngle = new Angle(finalYaw, finalPitch);
        RotationController.INSTANCE.setServerAngle(currentServerAngle);
        lastYaw = finalYaw;
        lastPitch = finalPitch;
    }
}