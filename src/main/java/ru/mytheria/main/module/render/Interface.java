package ru.mytheria.main.module.render;

import lombok.Getter;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.main.module.misc.Unhook;

@Getter
public class Interface extends Module {

    ModeListSetting visible = new ModeListSetting(Text.of("Показывать"), null, () -> true)
            .set("Watermark", "Keybinds", "Target");

    ModeSetting mode = new ModeSetting(Text.of("Тип"), null, () -> true)
            .set("Стекло", "Блюр")
            .setDefault("Блюр");

    ModeSetting theme = new ModeSetting(Text.of("Тема"), null, () -> true)
            .set("Тёмная", "Светлая")
            .setDefault("Тёмная");

    public Interface() {
        super(Text.of("Interface"), null, Category.RENDER);
        addSettings(visible, mode, theme);
    }

    @Override
    public void activate() {
        if (Unhook.ACTIVE) return; // ⬅ UI не включается
        super.activate();
    }
}
