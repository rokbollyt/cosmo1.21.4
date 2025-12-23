package ru.mytheria.main.module.render;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.api.events.impl.Render2DEvent;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BindSetting;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;

public class Test extends Module {

    SliderSetting minHealth = new SliderSetting(Text.of("test123"), null, () -> true)
            .set(1f, 19f, .5f);

    BooleanSetting showNames = new BooleanSetting(Text.of("test"), null, () -> true);

    BindSetting test = new BindSetting(Text.of("penis"), null, () -> true);

    public Test() {
        super(Text.of("Test"), null, Category.MISC);
        addSettings(test, minHealth, showNames);
    }
//нет
}
