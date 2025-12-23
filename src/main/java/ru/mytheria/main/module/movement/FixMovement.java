package ru.mytheria.main.module.movement;

import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class FixMovement extends Module {

    public FixMovement() {
        super(Text.of("FixMovement"), Category.MOVEMENT);
    }
}
