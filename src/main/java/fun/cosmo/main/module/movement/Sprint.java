package fun.cosmo.main.module.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.text.Text;
import fun.cosmo.api.events.impl.EventTick;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super(Text.of("Sprint"), null, Category.MOVEMENT);
    }
    @Subscribe
    public void onTick(EventTick e) {
        mc.player.setSprinting(true);
    }
}
