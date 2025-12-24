package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;
import ru.mytheria.main.module.combat.aura.points.MultiPointHandler;
import ru.mytheria.main.module.combat.aura.points.SmartPointHandler;
import ru.mytheria.main.module.combat.aura.rotation.Rotation;
import ru.mytheria.main.module.combat.aura.rotation.server.funtime.FuntimeRotation;
import ru.mytheria.main.module.combat.aura.util.TargetEntitySelector;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class AttackAura extends Module {
    TargetEntitySelector targetSelector = new TargetEntitySelector();
    MultiPointHandler multiPoint = new MultiPointHandler();
    SmartPointHandler smartPoint = new SmartPointHandler();
    FuntimeRotation funtimeRotation = new FuntimeRotation();

    @NonFinal
    LivingEntity target = null;
    @NonFinal
    Rotation currentRotation = null;

    private final ModeSetting rotate = new ModeSetting(Text.of("Ротация"), null, () -> true)
            .set("Polar", "Grim", "Snap", "Funtime", "None");

    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true)
            .set(2f, 6f, 1f);

    private final SliderSetting preAimDistance = new SliderSetting(Text.of("Пред-наводка"), null, () -> true)
            .set(1.0f, 6f, 0.1000000f);

    ModeListSetting targetTypeSetting = new ModeListSetting(Text.of("Таргет"), null, () -> true)
            .set(Stream.of(TargetEntitySelector.TargetType.values())
                    .map(TargetEntitySelector.TargetType::getDisplayName)
                    .toArray(String[]::new));

    private final ModeSetting points = new ModeSetting(Text.of("Тип поинтов"), null, () -> true)
            .set("Мультипоинты", "Смартпоинты");

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
    }

    @Override
    public void activate() {
        super.activate();
        currentRotation = null;
        System.out.println("[AURA] Funtime аура активирована!");
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        currentRotation = null;
        super.deactivate();
        System.out.println("[AURA] Funtime аура деактивирована!");
    }

    public void onTick() {
        // Используем геттер isEnabled() из Module (скорее всего он есть)
        if (!isEnabled()) return;

        // Обновление цели
        target = updateTarget();

        if (target == null) {
            currentRotation = null;
            return;
        }

        // Применение ротации в зависимости от выбранного режима
        if (rotate.getValue().equals("Funtime")) {
            applyFuntimeRotation();
        } else {
            // Твоя существующая логика для других ротаций
            applyOtherRotations();
        }

        // Атака цели если в радиусе
        if (canAttackTarget()) {
            attackTarget();
        }
    }

    private void applyFuntimeRotation() {
        if (funtimeRotation == null) {
            funtimeRotation = new FuntimeRotation();
        }

        // Получаем текущий угол
        net.minecraft.util.math.Vec2f currentVec = mc.player.getRotationClient();
        Angle currentAngle = AngleHandler.fromVec2f(currentVec);

        // Создаем ротацию через Funtime
        currentRotation = funtimeRotation.createFuntimeRotation(
                target,
                currentAngle,
                canAttackTarget()
        );

        // Применяем ротацию
        if (currentRotation != null) {
            Angle nextAngle = currentRotation.nextRotation(currentAngle, false);

            // Устанавливаем поворот игрока
            // В Rotation поле changeLook, а геттер getChangeLook()
            if (currentRotation.isChangeLook()) {
                mc.player.setYaw(nextAngle.getYaw());
                mc.player.setPitch(nextAngle.getPitch());
            }

            // Отправляем пакет на сервер
            sendRotationPacket(nextAngle);
        }
    }

    private void applyOtherRotations() {
        // Твоя существующая логика для Polar, Grim, Snap и None
        switch (rotate.getValue()) {
            case "Polar":
                // Твоя реализация Polar ротации
                break;
            case "Grim":
                // Твоя реализация Grim ротации
                break;
            case "Snap":
                // Твоя реализация Snap ротации
                break;
            case "None":
                // Без ротации
                currentRotation = null;
                break;
        }
    }

    private void sendRotationPacket(Angle angle) {
        // Отправка пакета ротации на сервер
        mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        angle.getYaw(),
                        angle.getPitch(),
                        mc.player.isOnGround(),
                        false
                )
        );
    }

    private boolean canAttackTarget() {
        if (target == null) return false;

        float distanceToTarget = mc.player.distanceTo(target);
        return distanceToTarget <= distance.getValue();
    }

    private void attackTarget() {
        // Базовая атака
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
    }

    private LivingEntity updateTarget() {
        Set<TargetEntitySelector.TargetType> targetTypes = new HashSet<>();

        for (String selected : targetTypeSetting.getSelected()) {
            for (TargetEntitySelector.TargetType type : TargetEntitySelector.TargetType.values()) {
                if (type.getDisplayName().equals(selected)) {
                    targetTypes.add(type);
                    break;
                }
            }
        }

        return targetSelector.updateTarget(targetTypes, mc.world.getEntities(), preAimDistance.getValue());
    }
}