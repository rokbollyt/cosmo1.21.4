package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.api.events.impl.TickEvent;
import ru.mytheria.api.events.impl.EventRender3D;
import ru.mytheria.api.events.impl.EventSync;
import ru.mytheria.api.events.impl.EventPostSync;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;
import org.joml.Vector2f;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

// Класс для хранения углов
record Turns(float yaw, float pitch) {}

// Класс для расчета разницы углов
class MathAngle {
    public static Turns calculateDelta(Vector2f current, Vector2f target) {
        float yawDelta = MathHelper.wrapDegrees(target.y - current.y);
        float pitchDelta = target.x - current.x;
        return new Turns(yawDelta, pitchDelta);
    }
}


@Getter
public class AttackAura extends Module {
    private static final net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
    private static final SecureRandom secureRandom = new SecureRandom();

    // --- НАСТРОЙКИ ---
    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true).set(3f, 6f, 0.1f).set(4.2f);
    private final SliderSetting rotationSpeed = new SliderSetting(Text.of("Скорость поворота"), null, () -> true).set(0.1f, 1.0f, 0.01f).set(0.95f);
    private final ModeSetting sprintMode = new ModeSetting(Text.of("Обход Спринта"), null, () -> true).set("None", "Legit").setDefault("Legit");
    private final BooleanSetting onlyCrits = new BooleanSetting(Text.of("Только криты"), null, () -> false).set(false);
    private final BooleanSetting targetPlayers = new BooleanSetting(Text.of("Игроки"), null, () -> true).set(true);
    private final BooleanSetting targetMobs = new BooleanSetting(Text.of("Мобы/Животные"), null, () -> true).set(false);

    // --- НОВАЯ НАСТРОЙКА ДЛЯ FUNTIME LOGIC ---
    private final SliderSetting rotationTolerance = new SliderSetting(Text.of("Точность поворота (Grim)"), null, () -> true).set(0.1f, 5.0f, 0.1f).set(1.5f);

    // --- СИНХРОНИЗАЦИЯ И РОТАЦИЯ ---
    @NonFinal
    private LivingEntity target = null;
    public static Vector2f headVector = new Vector2f(0f, 0f); // Расчетная ротация

    // Переменные для Spoof/Reset (Funtime Test Logic)
    static float spoofYaw, spoofPitch;
    @NonFinal
    private boolean isAuraSpoofing = false; // Состояние сброса ротации (Тик N)
    @NonFinal
    private boolean shouldAttackNextTick = false; // Флаг для атаки в N+1

    // Jitter/Prediction
    private long lastShakeTime = 0;
    private float targetVectorOffsetX = 0;
    private float targetVectorOffsetZ = 0;
    private float currentVectorOffsetX = 0;
    private float currentVectorOffsetZ = 0;
    private final long SHAKE_INTERVAL_MS = ThreadLocalRandom.current().nextLong(60, 180);

    // Attack Cooldown
    private final long MIN_ATTACK_DELAY_MS = 380;
    @NonFinal
    private long lastAttackTime = 0;
    private final float HIT_CHANCE_PERCENT = 95.0f;


    public AttackAura() {
        super(Text.of("AttackAura"), Category.COMBAT);
        this.addSettings(distance, rotationSpeed, sprintMode, onlyCrits, targetPlayers, targetMobs, rotationTolerance);
    }

    @Override
    public void activate() {
        super.activate();
        target = null;
        if (mc.player != null) {
            headVector = new Vector2f(mc.player.getPitch(), mc.player.getYaw());
        }
        lastAttackTime = 0;
        isAuraSpoofing = false;
        shouldAttackNextTick = false;
    }

    @Override
    public void deactivate() {
        target = null;
        // Восстанавливаем ротацию при выключении
        if (isAuraSpoofing && mc.player != null) {
            mc.player.setYaw(spoofYaw);
            mc.player.setPitch(spoofPitch);
        }
        isAuraSpoofing = false;
        shouldAttackNextTick = false;
        super.deactivate();
    }

    private float getGCDValue() {
        if (mc.options.getMouseSensitivity() == null) return 0.001f;
        float sens = (float) (mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        float t = sens * sens * sens * 8.0f;
        return t * 0.15f;
    }

    // =========================================================================
    // >>>>> 1. ROTATION LOGIC (onRender3D) <<<<<
    // =========================================================================

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null) return;

        target = updateTarget();

        if (target != null) {
            Vector2f idealTargetRot = calculateIdealRotation(target);
            applyRotation(idealTargetRot);
        }
    }

    private void applyRotation(Vector2f idealRotation) {
        if (mc.player == null) return;

        Turns angleDelta = MathAngle.calculateDelta(headVector, idealRotation);
        float yawDelta = angleDelta.yaw();
        float pitchDelta = angleDelta.pitch();

        float speed = rotationSpeed.getValue();

        float targetYawDelta = yawDelta * speed;
        float targetPitchDelta = pitchDelta * speed;

        float gcd = getGCDValue();
        targetYawDelta -= (targetYawDelta % gcd);
        targetPitchDelta -= (targetPitchDelta % gcd);

        float fixYaw = headVector.y + targetYawDelta;
        float fixPitch = headVector.x + targetPitchDelta;
        fixPitch = MathHelper.clamp(fixPitch, -90.0F, 90.0F);

        headVector = new Vector2f(fixPitch, MathHelper.wrapDegrees(fixYaw));
    }

    private Vector2f calculateIdealRotation(LivingEntity entity) {
        if (mc.player == null) return headVector;

        final float VECTOR_OFFSET_MUL = 0.14f;
        final float VECTOR_Y_OFFSET = -0.66f;
        final float VECTOR_LERP_SPEED = 0.26f;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShakeTime >= SHAKE_INTERVAL_MS) {
            targetVectorOffsetX = (secureRandom.nextFloat() * 2 - 1) * VECTOR_OFFSET_MUL;
            targetVectorOffsetZ = (secureRandom.nextFloat() * 2 - 1) * VECTOR_OFFSET_MUL;
            lastShakeTime = currentTime;
        }

        currentVectorOffsetX = MathHelper.lerp(VECTOR_LERP_SPEED, currentVectorOffsetX, targetVectorOffsetX);
        currentVectorOffsetZ = MathHelper.lerp(VECTOR_LERP_SPEED, currentVectorOffsetZ, targetVectorOffsetZ);

        Vec3d targetVelocity = entity.getVelocity();
        double predictionFactor = 0.1;
        Vec3d predictedPos = entity.getPos().add(targetVelocity.multiply(predictionFactor));

        Vec3d targetPos = predictedPos.add(
                currentVectorOffsetX,
                (entity.getHeight() * (0.5 + secureRandom.nextFloat() * 0.2)) + VECTOR_Y_OFFSET,
                currentVectorOffsetZ
        );

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d vec = targetPos.subtract(eyePos);
        double dist = vec.length();

        float finalYaw = (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float finalPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, dist)));

        finalYaw = MathHelper.wrapDegrees(finalYaw);

        return new Vector2f(MathHelper.clamp(finalPitch, -90.0F, 90.0F), finalYaw);
    }


    // =========================================================================
    // >>>>> 2. CORE GRIM BYPASS LOGIC (onTickEvent / onSync / onPostSync) <<<<<
    // =========================================================================

    /**
     * Проверяет, насколько близко HeadVector к текущей ротации игрока.
     */
    private boolean isRotationAligned() {
        if (mc.player == null || target == null) return false;

        // Расчет идеальной ротации
        Vector2f idealRot = calculateIdealRotation(target);

        // Разница между расчетной ротацией (headVector) и идеальной ротацией
        Turns delta = MathAngle.calculateDelta(headVector, idealRot);

        float tolerance = rotationTolerance.getValue();

        // Проверяем, что наша расчетная ротация близка к идеальной ротации
        return Math.abs(delta.yaw()) < tolerance && Math.abs(delta.pitch()) < tolerance;
    }


    private boolean shouldStartAttackSequence() {
        if (mc.player == null) return false;
        long currentTime = System.currentTimeMillis();

        boolean isClientCooldownReady = mc.player.getAttackCooldownProgress(0) >= 1.0f;
        boolean isServerCooldownReady = (currentTime - lastAttackTime) >= MIN_ATTACK_DELAY_MS;
        float currentDistance = (target != null) ? mc.player.distanceTo(target) : 0f;
        boolean canCrit = !mc.player.isOnGround() && !mc.player.isRiding();
        boolean critCheckPassed = !onlyCrits.getValue() || canCrit;

        // Добавляем проверку на наведение!
        boolean isAligned = isRotationAligned();

        return target != null
                && isAligned // Только если ротация вычислена
                && isClientCooldownReady
                && isServerCooldownReady
                && currentDistance <= distance.getValue()
                && critCheckPassed;
    }

    /**
     * EventSync: ТИК N. Сохраняет и применяет спуф-ротацию. Grim видит ротацию.
     */
    @EventHandler
    public void onSync(EventSync eventSync) {
        if (!this.isEnabled() || mc.player == null) return;

        // Если флаг атаки уже установлен, мы ждем N+1, не вмешиваемся
        if (shouldAttackNextTick) return;

        boolean shouldRotate = shouldStartAttackSequence();

        if (shouldRotate) {
            // Сохраняем ротацию
            if (!isAuraSpoofing) {
                spoofYaw = mc.player.getYaw();
                spoofPitch = mc.player.getPitch();
            }
            isAuraSpoofing = true;
            shouldAttackNextTick = true; // Планируем атаку в N+1

            // Применяем спуф-ротацию
            mc.player.setYaw(headVector.y);
            mc.player.setPitch(headVector.x);
        } else {
            // Сбрасываем флаги, если нет условий для атаки
            if (isAuraSpoofing) {
                isAuraSpoofing = false;
            }
            shouldAttackNextTick = false;
        }
    }

    /**
     * EventPostSync: ТИК N. Восстанавливает реальную ротацию после отправки пакета.
     */
    @EventHandler
    public void onPostSync(EventPostSync eventPostSync) {
        if (!this.isEnabled() || !isAuraSpoofing || mc.player == null) return;

        // Восстанавливаем ротацию
        mc.player.setYaw(spoofYaw);
        mc.player.setPitch(spoofPitch);
    }

    /**
     * TickEvent: ТИК N+1. Атакует. Grim считает, что ротация была в N-тике.
     */
    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null) return;

        if (shouldAttackNextTick && target != null) {
            // Атака в N+1 тике
            updateAttack(mc.player, target, System.currentTimeMillis());

            // Сброс флагов после успешной атаки
            shouldAttackNextTick = false;
            isAuraSpoofing = false;

            // Восстанавливаем углы (на всякий случай)
            mc.player.setYaw(spoofYaw);
            mc.player.setPitch(spoofPitch);
        }

        // Если флаг был установлен, но цель исчезла
        if (shouldAttackNextTick && target == null) {
            shouldAttackNextTick = false;
            isAuraSpoofing = false;
        }
    }

    private void updateAttack(ClientPlayerEntity player, LivingEntity target, long currentTime) {
        if (mc.interactionManager == null) return;

        // --- ОБХОД СПРИНТА (Legit Bypass) ---
        if (player.isSprinting() && sprintMode.getValue().equals("Legit")) {
            player.setSprinting(false);
        }

        // --- ЛОГИКА ПРОМАХА (Humanization) ---
        if (secureRandom.nextFloat() * 100 < (100 - HIT_CHANCE_PERCENT)) {
            float yawOffset = ThreadLocalRandom.current().nextFloat(10, 20);
            float pitchOffset = secureRandom.nextBoolean() ? 5 : -5;

            headVector = new Vector2f(
                    MathHelper.clamp(headVector.x + pitchOffset, -90, 90),
                    headVector.y + (secureRandom.nextBoolean() ? yawOffset : -yawOffset)
            );
        }

        // 1. Атака и Свинг (Grim Fix: ANIMATION должен быть сразу после INTERACT_ENTITY)
        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);

        // 2. Обновляем время последней атаки
        lastAttackTime = currentTime;
    }

    // ... (Метод updateTarget - оставлен без изменений)
    private LivingEntity updateTarget() {
        if (mc.player == null || mc.world == null) return null;

        float searchRange = distance.getValue() * 1.5f;

        if (target != null && mc.player.distanceTo(target) <= distance.getValue() + 1.0f) {
            return target;
        }

        return StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(entity -> entity instanceof LivingEntity livingEntity && livingEntity != mc.player)
                .map(entity -> (LivingEntity) entity)
                .filter(livingEntity -> {
                    if (livingEntity.isDead() || livingEntity.getHealth() <= 0) return false;

                    if (livingEntity instanceof PlayerEntity player) {
                        if (player.isSpectator() || player.getAbilities().creativeMode) return false;
                        if (!targetPlayers.getValue()) return false;
                    } else if (livingEntity instanceof Monster || livingEntity instanceof HostileEntity || livingEntity instanceof AnimalEntity) {
                        if (!targetMobs.getValue()) return false;
                    } else {
                        return false;
                    }

                    return mc.player.distanceTo(livingEntity) <= searchRange;
                })
                .min((e1, e2) -> Double.compare(mc.player.distanceTo(e1), mc.player.distanceTo(e2)))
                .orElse(null);
    }
}