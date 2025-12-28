package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import ru.mytheria.api.events.impl.*;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.impl.*;
import org.joml.Vector2f;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// =========================================================================
// >>>>> 0. УТИЛИТАРНЫЕ И ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ (ВОССТАНОВЛЕНЫ) <<<<<
// =========================================================================

// Утилитарный класс для подсчёта здоровья сущностей
class EntityCalculator {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static double health(LivingEntity entity) { return entity.getHealth() + entity.getAbsorptionAmount(); }
    public static double calculateEntityScore(LivingEntity entity, boolean health, boolean distance, double maxDistance) {
        double score = 1.0;
        if (health) score += health(entity);
        if (distance) score *= 1 + entity.distanceTo(mc.player) / maxDistance;
        return score;
    }
}

// Утилитарный класс для ракурсов
class RaytracingUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static boolean canSeeThroughWall(Entity entity) {
        if (mc.player == null || mc.world == null || entity == null) return false;
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetEyePos = entity.getEyePos();
        BlockHitResult hitResult = mc.world.raycast(new RaycastContext(eyePos, targetEyePos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return hitResult.getType() == BlockHitResult.Type.MISS;
    }
}

// Утилитарный класс для чувствительности мыши
class SensUtil {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static float getGCDValue() {
        if (mc.options.getMouseSensitivity() == null) return 0.001f;
        float sens = (float) (mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        float t = sens * sens * sens * 8.0f;
        return t * 0.15f;
    }
}

// Класс для хранения углов
record AngleDelta(float yawDelta, float pitchDelta) {}

// Утилитарный класс для расчёта углов
class MathAngle {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static AngleDelta calculateDelta(Vector2f current, Vector2f target) {
        float yawDelta = MathHelper.wrapDegrees(target.y - current.y);
        float pitchDelta = target.x - current.x;
        return new AngleDelta(yawDelta, pitchDelta);
    }

    public static Vector2f calculateAnglesToTarget(Entity target) {
        if (mc.player == null) return new Vector2f(0, 0);
        double deltaX = target.getX() - mc.player.getX();
        double deltaY = (target.getEyeY() - mc.player.getEyeY());
        double deltaZ = target.getZ() - mc.player.getZ();
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);
        yaw = MathHelper.wrapDegrees(yaw);
        return new Vector2f(pitch, yaw);
    }
}

// Класс для управления атакой (Минимально изменен)
class AttackHandler {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean canPerformCriticals() {
        if (mc.player == null) return false;
        BlockPos feetPos = mc.player.getBlockPos();
        BlockPos headPos = feetPos.up();
        boolean isInCobweb = mc.world.getBlockState(feetPos).isOf(Blocks.COBWEB) || mc.world.getBlockState(headPos).isOf(Blocks.COBWEB);

        if (mc.player.getMainHandStack().getItem() instanceof MaceItem) return true;

        return !isInCobweb &&
                !mc.player.hasStatusEffect(StatusEffects.BLINDNESS) &&
                !mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) &&
                !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING) &&
                !mc.player.isSubmergedInWater() &&
                !mc.player.isTouchingWater() &&
                !mc.player.isInLava() &&
                !mc.player.isClimbing() &&
                !mc.player.isOnGround() &&
                mc.player.fallDistance > 0;
    }
}

// Селектор целей
class TargetSelector {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Set<UUID> friends = new HashSet<>();

    public static LivingEntity findTarget(float range, boolean targetPlayers, boolean targetMobs, boolean targetAnimals) {
        if (mc.player == null || mc.world == null) return null;

        Box searchBox = mc.player.getBoundingBox().expand(range);
        List<LivingEntity> validTargets = new ArrayList<>();

        for (Entity entity : mc.world.getEntitiesByClass(LivingEntity.class, searchBox, e ->
                isValidTarget((LivingEntity) e, range, targetPlayers, targetMobs, targetAnimals))) {
            validTargets.add((LivingEntity) entity);
        }

        if (validTargets.isEmpty()) return null;
        validTargets.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        return validTargets.get(0);
    }

    private static boolean isValidTarget(LivingEntity entity, float range,
                                         boolean targetPlayers, boolean targetMobs, boolean targetAnimals) {
        if (entity == mc.player) return false;
        if (!entity.isAlive() || entity.isInvulnerable()) return false;
        if (mc.player.distanceTo(entity) > range) return false;

        if (entity instanceof PlayerEntity) {
            if (!targetPlayers) return false;
            if (friends.contains(entity.getUuid())) return false;
            PlayerEntity player = (PlayerEntity) entity;
            if (player.isSpectator() || player.isCreative()) return false;
        } else if (entity instanceof Monster || entity instanceof HostileEntity || entity instanceof SquidEntity) {
            if (!targetMobs) return false;
        } else if (entity instanceof AnimalEntity) {
            if (!targetAnimals) return false;
            if (entity instanceof TameableEntity tameable) {
                if (tameable.isTamed() && tameable.getOwnerUuid() != null &&
                        tameable.getOwnerUuid().equals(mc.player.getUuid())) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }
}

// =========================================================================
// >>>>> 1. ИСПРАВЛЕННЫЙ ОСНОВНОЙ КЛАСС (AttackAura) <<<<<
// =========================================================================

@Getter
public class AttackAura extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final SecureRandom secureRandom = new SecureRandom();

    // --- НАСТРОЙКИ (Ваши настройки) ---
    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), Text.of("Дистанция атаки"), () -> true).set(3f, 6f, 0.1f).set(4.2f);
    private final SliderSetting rotationSpeed = new SliderSetting(Text.of("Скорость поворота"), Text.of("Скорость наведения"), () -> true).set(0.1f, 1.0f, 0.01f).set(0.95f);
    private final ModeSetting rotationMode = new ModeSetting(Text.of("Режим ротации"), Text.of("Тип ротации"), () -> true).set("Плавная", "HvH", "FunTime", "SpookyTime V1").setDefault("FunTime");
    private final ModeSetting attackMode = new ModeSetting(Text.of("Режим атаки"), Text.of("Механика атаки"), () -> true).set("1.9", "1.8").setDefault("1.9");
    private final SliderSetting cps = new SliderSetting(Text.of("CPS"), Text.of("Кликов в секунду"), () -> attackMode.getValue().equals("1.8")).set(1f, 20f, 0.5f).set(12f);
    private final BooleanSetting onlyCrits = new BooleanSetting(Text.of("Только криты"), Text.of("Атаковать только с критами"), () -> true).set(false);
    private final BooleanSetting targetPlayers = new BooleanSetting(Text.of("Игроки"), Text.of("Атаковать игроков"), () -> true).set(true);
    private final BooleanSetting targetMobs = new BooleanSetting(Text.of("Мобы"), Text.of("Атаковать мобов"), () -> true).set(false);
    private final BooleanSetting targetAnimals = new BooleanSetting(Text.of("Животные"), Text.of("Атаковать животных"), () -> false).set(false);
    private final BooleanSetting attackThroughBlocks = new BooleanSetting(Text.of("Бить через блоки"), Text.of("Атаковать через стены"), () -> true).set(false);
    private final BooleanSetting onlyWeapon = new BooleanSetting(Text.of("Только оружие"), Text.of("Только с оружием"), () -> false).set(false);
    private final BooleanSetting funTimeBypass = new BooleanSetting(Text.of("FunTime Bypass"), Text.of("Обход FunTime (доп. смещение)"), () -> true).set(true);

    private final List<Setting> settings = new ArrayList<>();

    // --- ПЕРЕМЕННЫЕ (Ваши переменные) ---
    @NonFinal private LivingEntity target = null;
    public static Vector2f headVector = new Vector2f(0f, 0f);
    @NonFinal private float spoofYaw, spoofPitch;
    @NonFinal private boolean isAuraSpoofing = false;
    @NonFinal private boolean shouldAttack = false; // <<< ФЛАГ ДЛЯ АТАКИ
    @NonFinal private boolean shouldRestoreAngles = false; // <<< ФЛАГ ДЛЯ ВОССТАНОВЛЕНИЯ

    private long lastAttackTime = 0;
    private int attackTicks = 0;
    private int funtimeTicks = 0;
    private int tickSinceTicks = 0;
    private long funTimeTimerMS = 0;

    public AttackAura() {
        super(Text.of("AttackAura"), Category.COMBAT);
        initSettings();
    }

    private void initSettings() {
        settings.add(distance); settings.add(rotationSpeed); settings.add(rotationMode);
        settings.add(attackMode); settings.add(cps); settings.add(onlyCrits);
        settings.add(targetPlayers); settings.add(targetMobs); settings.add(targetAnimals);
        settings.add(attackThroughBlocks); settings.add(onlyWeapon); settings.add(funTimeBypass);
    }

    @Override
    public void activate() {
        super.activate();
        target = null;
        if (mc.player != null) headVector = new Vector2f(mc.player.getPitch(), mc.player.getYaw());
        funTimeTimerMS = System.currentTimeMillis();
        tickSinceTicks = 0;
        isAuraSpoofing = false;
        shouldAttack = false;
        shouldRestoreAngles = false;
    }

    @Override
    public void deactivate() {
        target = null;
        if (isAuraSpoofing && mc.player != null) {
            mc.player.setYaw(spoofYaw);
            mc.player.setPitch(spoofPitch);
        }
        isAuraSpoofing = false;
        shouldAttack = false;
        shouldRestoreAngles = false;
        super.deactivate();
    }

    // --- ВАШ МЕТОД: rotation(float, float) ---
    private void rotation(float rotationYawSpeed, float rotationPitchSpeed) {
        if (target == null || mc.player == null) return;

        Vec3d targetPos = target.getEyePos();
        Vec3d playerPos = mc.player.getEyePos();

        Vec3d vec = targetPos.subtract(playerPos).add(
                0,
                MathHelper.clamp(mc.player.getEyePos().y - targetPos.y, 0, target.getHeight() * (mc.player.getPos().distanceTo(targetPos) / distance.getValue())) - 0.44f,
                0
        );

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z))));
        pitchToTarget = MathHelper.clamp(pitchToTarget, -90f, 90f);

        float yawDelta = MathHelper.wrapDegrees(yawToTarget - headVector.y);
        float pitchDelta = MathHelper.wrapDegrees(pitchToTarget - headVector.x);

        if (rotationMode.getValue().equals("FunTime")) {
            long timeElapsed = System.currentTimeMillis() - funTimeTimerMS;

            float yawSpeed = (funtimeTicks < 1 || mc.player.isUsingItem()) ?
                    ThreadLocalRandom.current().nextFloat(0.1f, 1.5f) : ThreadLocalRandom.current().nextFloat(900, 1600);
            float pitchSpeed = (timeElapsed < 270 || mc.player.isUsingItem()) ? 6 : 20;

            float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 0f), yawSpeed);
            float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 0f), pitchSpeed);

            float yaw = headVector.y + (yawDelta > 0 ? clampedYaw : -clampedYaw);
            float pitch = headVector.x + (pitchDelta > 0 ? clampedPitch : -clampedPitch);

            float maxYawChange = mc.player.distanceTo(target) < 1.7 ? 44 : 57.0f;
            float maxPitchChange = 15.5f;
            float ceilValue = ThreadLocalRandom.current().nextFloat(8, 9);

            float lerpValue = 1f;
            float pitchlerp = 0.5f;

            if (timeElapsed > 205 && timeElapsed < 392) {
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
                lerpValue = ThreadLocalRandom.current().nextFloat(0.7f, 1f);
                pitchlerp = 0.3f;
            }

            if (timeElapsed > 400) {
                funTimeTimerMS = System.currentTimeMillis();
                lerpValue = 1f;
            }

            if (mc.player.isOnGround() && tickSinceTicks > 35 && onlyCrits.getValue()) {
                lerpValue = ThreadLocalRandom.current().nextFloat(0.4f, 0.6f);
                yaw = mc.player.getYaw();
                pitch = mc.player.getPitch();
            }

            yaw = headVector.y + MathHelper.clamp(yaw - headVector.y, -maxYawChange, maxYawChange);
            pitch = MathHelper.clamp(headVector.x + MathHelper.clamp(pitch - headVector.x, -maxPitchChange, maxPitchChange), -89.0F, 89.0F);

            pitch = MathHelper.lerp(pitchlerp, headVector.x, pitch);
            yaw = MathHelper.lerp(lerpValue, headVector.y, yaw);

            yaw += (float) (Math.ceil(ceilValue * Math.cos(System.currentTimeMillis() / 55D)));
            pitch += (float) (Math.ceil(MathHelper.lerp(ThreadLocalRandom.current().nextFloat(), 2, 7) * Math.sin(System.currentTimeMillis() / 80D)));

            if (attackTicks == 0 && attackThroughBlocks.getValue() && funTimeBypass.getValue() && target != null && !RaytracingUtil.canSeeThroughWall(target)) {
                yaw += ThreadLocalRandom.current().nextFloat(17, 24);
            }

            float gcd = SensUtil.getGCDValue();
            yaw -= (yaw - headVector.y) % gcd;
            pitch -= (pitch - headVector.x) % gcd;

            headVector = new Vector2f(MathHelper.clamp(pitch, -90, 90), yaw);

        } else if (rotationMode.getValue().equals("Плавная")) {
            float speed = secureRandom.nextFloat(125, 140) * rotationSpeed.getValue();
            float cYaw = Math.min(Math.max(Math.abs(yawDelta), 0), speed);
            float cPitch = Math.min(Math.max(Math.abs(pitchDelta), 0f), 20);
            float yaw = headVector.y + (yawDelta > 0 ? cYaw : -cYaw);
            float pitch = MathHelper.clamp(headVector.x + (pitchDelta > 0 ? cPitch : -cPitch), -90, 90);
            headVector = new Vector2f(pitch, yaw);
        } else {
            // Default/HvH logic
            headVector = new Vector2f(pitchToTarget, yawToTarget);
        }
    }

    // --- ВАШ МЕТОД: canAttack() ---
    private boolean canAttackCheck() {
        if (mc.player == null || target == null) return false;
        if (mc.player.distanceTo(target) > distance.getValue()) return false;
        if (!attackThroughBlocks.getValue() && !RaytracingUtil.canSeeThroughWall(target)) return false;
        if (onlyCrits.getValue() && !AttackHandler.canPerformCriticals()) return false;
        if (onlyWeapon.getValue() && !isHoldingWeapon()) return false;

        if (attackMode.getValue().equals("1.9")) {
            return mc.player.getAttackCooldownProgress(0.5f) >= 0.9f;
        } else {
            long delay = (long) (1000.0 / cps.getValue());
            return (System.currentTimeMillis() - lastAttackTime) >= delay;
        }
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null) return false;
        var item = mc.player.getMainHandStack().getItem();
        return item instanceof SwordItem || item instanceof AxeItem || item instanceof MaceItem || item instanceof TridentItem;
    }


    // =========================================================================
    // >>>>> 2. ИСПРАВЛЕННАЯ ЛОГИКА СИНХРОНИЗАЦИИ И АТАКИ <<<<<
    // =========================================================================

    // Событие Render3D/Tick (ОБНОВЛЕНИЕ ЦЕЛИ И РОТАЦИИ)
    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // 1. Поиск цели и Ротация (выполняется постоянно)
        target = TargetSelector.findTarget(distance.getValue(), targetPlayers.getValue(), targetMobs.getValue(), targetAnimals.getValue());
        if (target != null) {
            rotation(60, 30); // Расчет headVector
        }

        // 2. Обновление флага атаки
        shouldAttack = (target != null && canAttackCheck());

        // 3. Обновление логики тиков
        float attackCd = mc.player.getAttackCooldownProgress(0.5f);
        if (attackMode.getValue().equals("1.9")) {
            funtimeTicks = (attackCd >= 0.69 || attackCd <= 0.11) ? 1 : 0;
        } else {
            funtimeTicks = 1;
        }
        attackTicks = (System.currentTimeMillis() - lastAttackTime < 50) ? 1 : 0;
        if (mc.player.isOnGround() || !AttackHandler.canPerformCriticals()) {
            tickSinceTicks++;
        } else {
            tickSinceTicks = 0;
        }

        // 4. Исполнение отложенной атаки
        if (shouldRestoreAngles) {
            // Если в прошлом Sync были применены спуф-углы, но атаки не было, мы должны их сбросить
            // Если атака БЫЛА, то сброс происходит в onPostSync
            if (!isAuraSpoofing) { // Проверяем, не запущена ли новая ротация в этом же тике
                mc.player.setYaw(spoofYaw);
                mc.player.setPitch(spoofPitch);
                mc.player.setHeadYaw(spoofYaw);
                mc.player.setBodyYaw(spoofYaw);
                shouldRestoreAngles = false;
            }
        }
    }

    // Событие Sync (ОТПРАВКА СЕРВЕРУ ПАКЕТА С УГЛАМИ)
    @EventHandler
    public void onSync(EventSync eventSync) {
        if (mc.player == null) return;

        if (shouldAttack) {
            if (!isAuraSpoofing) {
                spoofYaw = mc.player.getYaw();
                spoofPitch = mc.player.getPitch();
            }
            isAuraSpoofing = true;
            shouldRestoreAngles = true;

            // Применяем углы для пакета (этот пакет пойдет на сервер)
            mc.player.setYaw(headVector.y);
            mc.player.setPitch(headVector.x);
            mc.player.setHeadYaw(headVector.y);
            mc.player.setBodyYaw(headVector.y);

            // Атака происходит в этом же тике, после отправки пакета движения.
            // НЕ РЕКОМЕНДУЕТСЯ, но для простоты и обхода, атакуем здесь.
            // *Для большинства анти-читов, атака должна происходить в onTick*

            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttackTime = System.currentTimeMillis();

            if (rotationMode.getValue().equals("FunTime")) {
                // Сброс таймера FunTime при ударе
                funTimeTimerMS = System.currentTimeMillis();
            }
        } else {
            isAuraSpoofing = false;
        }
    }

    // Событие PostSync (ВОССТАНОВЛЕНИЕ УГЛОВ КЛИЕНТА)
    @EventHandler
    public void onPostSync(EventPostSync event) {
        if (isAuraSpoofing && mc.player != null) {
            // Восстанавливаем углы сразу после отправки пакета движения
            mc.player.setYaw(spoofYaw);
            mc.player.setPitch(spoofPitch);
            mc.player.setHeadYaw(spoofYaw);
            mc.player.setBodyYaw(spoofYaw);
            isAuraSpoofing = false;
            shouldRestoreAngles = false; // Сброс флага, т.к. восстановление произошло
        }
    }

    @Override
    public List<Setting> getSettingLayers() { return settings; }
}