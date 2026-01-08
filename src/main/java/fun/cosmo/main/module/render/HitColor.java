package fun.cosmo.main.module.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import fun.cosmo.Mytheria;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.impl.SliderSetting; // 🔥 Новый импорт
import fun.cosmo.api.util.color.ColorUtil;
import fun.cosmo.api.module.settings.Setting;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HitColor extends Module {

    // 🔥 Новая настройка для регулировки прозрачности HitColor
    private static final SliderSetting alphaSetting = new SliderSetting(
            Text.of("Альфа"),
            Text.of("Прозрачность цвета при получении урона"),
            () -> true
    ).set(0.1F, 1F, 0.01F).set(0.8f); // Устанавливаем по умолчанию 0.8f (менее прозрачный)

    public int color = ColorUtil.getClientColor();

    public static HitColor getInstance() {
        return (HitColor) Mytheria.getInstance().getModuleManager().getModule("HitColor");
    }

    // 🔥 Метод для получения значения Альфа
    public float getAlphaValue() {
        return alphaSetting.getValue();
    }

    public boolean isActive() {
        return this.isEnabled();
    }

    public HitColor() {
        super(Text.of("HitColor"), Text.of("При ударе окрашивает сущность"), Category.RENDER);
        addSettings(alphaSetting); // 🔥 Добавляем настройку
    }

    @Override
    public List<Setting> getSettingLayers() {
        return super.getSettingLayers();
    }
}