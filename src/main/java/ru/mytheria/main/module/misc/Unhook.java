package ru.mytheria.main.module.misc;

import net.minecraft.text.Text;
import ru.mytheria.Mytheria;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class Unhook extends Module {

    public static boolean ACTIVE = false;

    public Unhook() {
        super(Text.of("Unhook"), Category.MISC);
        setKey(-1); // только GUI
    }

    @Override
    public void activate() {
        ACTIVE = true;

        // выключаем ВСЕ модули кроме Unhook
        Mytheria.getInstance().getModuleManager().forEach(module -> {
            if (module != this && module.getEnabled()) {
                module.setEnabled(false);
            }
        });

        // отписываем менеджер от событий (бинды, тики, эвенты)
        Mytheria.getInstance()
                .getEventProvider()
                .unsubscribe(Mytheria.getInstance().getModuleManager());

        super.activate();
    }

    @Override
    public void deactivate() {
        ACTIVE = false;

        // возвращаем менеджер
        Mytheria.getInstance()
                .getEventProvider()
                .subscribe(Mytheria.getInstance().getModuleManager());

        super.deactivate();
    }
}
