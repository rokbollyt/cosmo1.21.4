package ru.mytheria.main.module.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.text.Text;
import ru.mytheria.api.events.impl.EventTick;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super(Text.of("Sprint"), null, Category.MOVEMENT);
    }
    @Subscribe
    public void onTick(EventTick e) {
        mc.player.setSprinting(true);
    }
}
