package ru.mytheria.main.module.movement;

import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.SliderSetting;

public class ElytraForward extends Module {

    public final SliderSetting forward = new SliderSetting(Text.of("Форвард"), null, () -> true).set(3f, 6f, 1f);

    public ElytraForward() {
        super(Text.of("ElytraForward"), Category.MOVEMENT);
    }
}