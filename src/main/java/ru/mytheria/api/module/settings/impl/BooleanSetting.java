package ru.mytheria.api.module.settings.impl;

/*
 * Create by puzatiy
 * At 03.06.2025
 */

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.util.animations.Direction;

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
        this.enabled = enabled;
        this.getAnimation().setDirection(this.enabled ? Direction.FORWARDS : Direction.BACKWARDS);
        this.getAnimation().reset();

        return this;
    }

 /*   @Override
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
