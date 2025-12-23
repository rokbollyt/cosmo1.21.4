package ru.mytheria.main.module.misc;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.mytheria.Mytheria;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class Unhook extends Module {

    public static boolean ACTIVE = false;

    public Unhook() {
        super(Text.of("Unhook"), Category.MISC);
        setKey(-1); // только через GUI
    }

    @Override
    public void activate() {
        ACTIVE = true;

        // 1. Выключаем ВСЕ модули кроме себя
        Mytheria.getInstance().getModuleManager().forEach(module -> {
            if (module != this && module.getEnabled()) {
                module.setEnabled(false);
            }
        });

        // 2. ПРИНУДИТЕЛЬНО закрываем любой GUI
        if (mc.currentScreen != null) {
            mc.setScreen(null);
        }

        super.activate();
    }

    @Override
    public void deactivate() {
        ACTIVE = false;
        super.deactivate();
    }
}
