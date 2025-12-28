package ru.mytheria.main.module.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.impl.SliderSetting;
import ru.mytheria.api.events.impl.EntityColorEvent;
import ru.mytheria.api.util.color.ColorUtil;

import java.util.List;

public class SeeInvisible extends Module {

    private static final SliderSetting alphaSetting = new SliderSetting(
            Text.of("Альфа"),
            Text.of("Прозрачность для невидимых сущностей"),
            () -> true
    ).set(0.1F, 1F, 0.01F).set(0.5f);

    public SeeInvisible() {
        super(Text.of("SeeInvisible"), Category.RENDER);
        addSettings(alphaSetting);
    }

    public float getAlphaValue() {
        return alphaSetting.getValue();
    }

    @EventHandler
    public static void onEntityColor(EntityColorEvent e) {
        if (e.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                float alphaValue = alphaSetting.getValue();
                int opacityPercent = (int) (alphaValue * 100);
                e.setColor(ColorUtil.applyOpacity(e.getColor(), opacityPercent));
                e.setCancelled(true);
            }
        }
    }

    @Override
    public List<Setting> getSettingLayers() {
        return super.getSettingLayers();
    }
}