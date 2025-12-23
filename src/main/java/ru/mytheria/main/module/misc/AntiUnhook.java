package ru.mytheria.main.module.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.text.Text;
import ru.mytheria.api.events.impl.EventTick;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class AntiUnhook extends Module {

    public AntiUnhook() {
        super(Text.of("AntiUnhook"), null, Category.MISC);
        setKey(-1);
    }

    @Subscribe
    public void onTick(EventTick e) {
        if (fullNullCheck()) return;

        // Сброс таймера AFK / unhook
        mc.player.age = 0;
    }
}
