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
import ru.mytheria.main.module.combat.aura.points.MultiPointHandler;
import ru.mytheria.main.module.combat.aura.points.SmartPointHandler;
import ru.mytheria.main.module.combat.aura.util.TargetEntitySelector;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class AttackAura extends Module {
    TargetEntitySelector targetSelector = new TargetEntitySelector();
    MultiPointHandler multiPoint = new MultiPointHandler();
    SmartPointHandler smartPoint = new SmartPointHandler();

    @NonFinal
    LivingEntity target = null;

    private final ModeSetting rotate = new ModeSetting(Text.of("Ротация"), null, () -> true).set("Polar", "Grim", "Snap", "None");

    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true).set(2f, 6f, 1f);

    private final SliderSetting preAimDistance = new SliderSetting(Text.of("Пред-наводка"), null, () -> true).set(1.0f, 6f, 0.1000000f);


    ModeListSetting targetTypeSetting = new ModeListSetting(Text.of("Таргет"), null, () -> true).set(Stream.of(TargetEntitySelector.TargetType.values())
            .map(TargetEntitySelector.TargetType::getDisplayName)
            .toArray(String[]::new));

    private final ModeSetting points = new ModeSetting(Text.of("Тип поинтов"), null, () -> true).set("Мультипоинты", "Смартпоинты");

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        super.deactivate();
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
