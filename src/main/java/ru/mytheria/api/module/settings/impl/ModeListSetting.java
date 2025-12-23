package ru.mytheria.api.module.settings.impl;

/*
 * Create by puzatiy
 * At 03.06.2025
 */

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeListSetting extends Setting {

    @Getter
    List<BooleanSetting> values = new ArrayList<>();

    public ModeListSetting(Text name, Text description, Supplier<Boolean> visible) {
        super(name, description, visible);
    }

    public ModeListSetting set(String... list) {
        Arrays.stream(list).map(this::create).forEach(values::add);

        return this;
    }

    public boolean empty() {
        return values.isEmpty();
    }

    public boolean emptySelected() {
        return values.stream().filter(BooleanSetting::getEnabled).toList().isEmpty();
    }

    public List<String> asStringList() {
        return values.stream().map(BooleanSetting::getName).map(Text::getString).toList();
    }

    public List<String> getSelected() {
        return values.stream().filter(BooleanSetting::getEnabled).map(BooleanSetting::getName).map(Text::getString).toList();
    }

    public BooleanSetting get(String text) {
        return values.stream()
                .filter(e -> e.getName().getString().equalsIgnoreCase(text))
                .findFirst()
                .orElse(null);
    }


    public BooleanSetting create(String text) {
        return new BooleanSetting(Text.of(text), Text.empty(), () -> true);
    }

   /* @Override
    public ModeListSetting register( Module provider) {
        super.reg(provider);

        return this;
    }*/

    @Override
    public ModeListSetting collection(Collection collection) {
        collection.put(this);

        return this;
    }
}
