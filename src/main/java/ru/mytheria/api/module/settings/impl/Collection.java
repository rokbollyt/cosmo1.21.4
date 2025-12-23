package ru.mytheria.api.module.settings.impl;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Collection extends Setting {

    List<Setting> settingLayers = new ArrayList<>();

    public Collection(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);
    }

    public Collection put(Setting settingLayer) {
        settingLayers.add(settingLayer);
        return this;
    }

    public Collection register(Module provider) {
        provider.getSettingLayers().add(this);
        return this;
    }

    @Override
    public Setting collection(Collection collection) {
        collection.put(this);
        return this;
    }
}
