package ru.mytheria.api.module.settings.impl;




import lombok.Getter;
import lombok.Setter;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.SettingApi;
import ru.mytheria.api.util.animations.Direction;

import java.util.Objects;
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
        this.key = keyCode;
        this.setSelected(false);
        this.getAnimation().setDirection(this.selected ? Direction.FORWARDS : Direction.BACKWARDS);

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
