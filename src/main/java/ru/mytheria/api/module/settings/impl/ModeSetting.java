package ru.mytheria.api.module.settings.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeSetting extends Setting {

    List<String> values = new ArrayList<>();

    @NonFinal
    Boolean opened = false;

    @NonFinal
    String value = null;

    @NonFinal
    String defaultValue = null;

    Animation openAnimation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    public ModeSetting(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);

        openAnimation.setDirection(this.opened ? Direction.FORWARDS : Direction.BACKWARDS);
        getAnimation().setMs(400);
    }

    public ModeSetting setDefault(String defaultValue) {
        this.defaultValue = defaultValue;

        if (this.value == null) {
            this.value = defaultValue;
        }

        return this;
    }

    public ModeSetting setOpened(Boolean opened) {
        this.opened = opened;
        this.openAnimation.setDirection(this.opened ? Direction.FORWARDS : Direction.BACKWARDS);
        this.openAnimation.reset();

        return this;
    }

    public ModeSetting set(String... strings) {
        values.addAll(Arrays.asList(strings));

        if (defaultValue != null && value == null) {
            this.value = defaultValue;
        }

        return this;
    }

    public ModeSetting set(String value) {
        if (this.value != null && this.value.equalsIgnoreCase(value)) return this;

        this.value = value;
        this.getAnimation().reset();

        return this;
    }

    @Override
    public ModeSetting collection(Collection collection) {
        collection.put(this);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof String) || value == null) return false;
        return value.equalsIgnoreCase((String) obj);
    }
}
