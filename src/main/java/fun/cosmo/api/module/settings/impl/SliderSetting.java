package fun.cosmo.api.module.settings.impl;

/*
 * Create by puzatiy
 * At 03.06.2025
 */

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.util.SoundUtil; // <-- ИМПОРТ

import java.util.function.Supplier;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SliderSetting extends Setting {

    Float value, min, max, increments;

    @Setter
    Boolean dragging = false;

    public SliderSetting( Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);
    }

    public SliderSetting set(float min, float max, float increments) {
        this.min = min;
        this.max = max;
        this.increments = increments;
        this.set(max / 2);

        return this;
    }

    public SliderSetting set(float value) {
        // Проверка на изменение
        if (this.value != null && this.value.equals(value)) return this;

        this.value = value;

        SoundUtil.playSwipeSound();
        // ------------------------------------

        return this;
    }

  /* @Override
    public SliderSetting register( Module provider) {
        super.reg(provider);

        return this;
    }*/

    @Override
    public SliderSetting collection(Collection collection) {
        collection.put(this);

        return this;
    }
}