package fun.cosmo.api.module.settings.impl;


import lombok.Getter;
import net.minecraft.text.Text;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.util.animations.Direction;
import fun.cosmo.api.util.SoundUtil; // <-- ИМПОРТ

import java.util.function.Supplier;

@Getter
public class BindSetting extends Setting {

    Integer key = -1;
    Boolean selected = false;

    public BindSetting(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);

        this.getAnimation().setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public BindSetting set(Integer keyCode) {
        if (this.key.equals(keyCode)) return this; // Проверка на изменение

        this.key = keyCode;
        this.setSelected(false);
        this.getAnimation().setDirection(this.selected ? Direction.FORWARDS : Direction.BACKWARDS);

        // --- ВОСПРОИЗВЕДЕНИЕ ВАШЕГО ЗВУКА ---
        SoundUtil.playSwipeSound();
        // ------------------------------------

        return this;
    }

    public BindSetting setSelected(boolean selected) {
        this.selected = selected;
        this.getAnimation().setDirection(this.selected ? Direction.FORWARDS : Direction.BACKWARDS);
        this.getAnimation().reset();

        return this;
    }

    @Override
    public BindSetting collection(Collection collection) {
        collection.put(this);

        return this;
    }
}