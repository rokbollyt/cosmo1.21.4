package fun.cosmo.api.util.color;

import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import fun.cosmo.api.module.settings.Setting; // Наследуем от вашего абстрактного класса

import java.awt.Color;
import java.util.function.Supplier;

// ColorSetting должен находиться в пакете impl, как и другие ваши настройки
public abstract class ColorSetting extends Setting {

    // Хранение цвета в HSB и Alpha для GUI-ползунков
    private float hsb, saturation, brightness, alpha;

    /**
     * Конструктор, соответствующий API родительского класса Setting.
     * @param name Название настройки.
     * @param description Описание настройки.
     * @param visible Поставщик для проверки видимости.
     */
    public ColorSetting(Text name, Text description, Supplier<Boolean> visible) {
        // Вызываем конструктор родительского класса Setting
        super(name, description, visible);

        // Инициализация текущим цветом из ColorUtil
        this.set(ColorUtil.getClientColor());
    }

    /**
     * Устанавливает цвет из HSB и Alpha (используется GUI).
     * Обновляет статическое значение в ColorUtil.
     */
    public ColorSetting set(float hsb, float saturation, float brightness, float alpha) {
        this.hsb = hsb;
        this.saturation = saturation;
        this.brightness = brightness;
        this.alpha = alpha;

        // 1. Конвертируем HSB в RGB int
        int rgb = Color.HSBtoRGB(hsb, saturation, brightness);

        // 2. Добавляем Alpha (0.0-1.0 -> 0-255)
        int argb = ColorHelper.getArgb((int) (alpha * 255), ColorHelper.getRed(rgb), ColorHelper.getGreen(rgb), ColorHelper.getBlue(rgb));

        // 3. Обновляем статический цвет клиента
        ColorUtil.setClientColor(argb);

        return this;
    }

    /**
     * Инициализирует внутренние поля из int ARGB цвета.
     */
    public ColorSetting set(int color) {
        float[] hsbValue = Color.RGBtoHSB(ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color), new float[3]);

        this.hsb = hsbValue[0];
        this.saturation = hsbValue[1];
        this.brightness = hsbValue[2];
        this.alpha = ColorUtil.alpha(color) / 255.0f;

        return this;
    }

    // --- Геттеры для GUI ---
    public float getHsb() { return hsb; }
    public float getSaturation() { return saturation; }
    public float getBrightness() { return brightness; }
    public float getAlpha() { return alpha; }

    public int getCurrentColor() {
        return ColorUtil.getClientColor();
    }

    // ВАЖНО: Метод collection() был удален, так как он не является частью
    // предоставленного API Setting. Если этот метод требуется, его нужно
    // добавить в SettingApi и реализовать здесь.
}