package fun.cosmo.api.module.settings.impl;

/*
 * Create by puzatiy
 * At 03.06.2025
 */

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.util.animations.Direction;
import fun.cosmo.api.util.SoundUtil; // <-- ИМПОРТ

import java.util.function.Supplier;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BooleanSetting extends Setting {

    Boolean enabled = false;

    public BooleanSetting(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);

        this.getAnimation().setDirection(this.enabled ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public BooleanSetting set(Boolean enabled) {
        // Проверка на изменение
        if (this.enabled.equals(enabled)) return this;

        this.enabled = enabled;
        this.getAnimation().setDirection(this.enabled ? Direction.FORWARDS : Direction.BACKWARDS);
        this.getAnimation().reset();

        // --- ВОСПРОИЗВЕДЕНИЕ ВАШЕГО ЗВУКА ---
        SoundUtil.playSwipeSound();
        // ------------------------------------

        return this;
    }

 /* @Override
    public BooleanSetting register( Module provider) {
        super.reg(provider);

        return this;
    }*/

    // Добавить этот метод!
    public boolean getValue() {
        return enabled;
    }
    @Override
    public BooleanSetting collection(Collection collection) {
        collection.put(this);

        return this;
    }
}