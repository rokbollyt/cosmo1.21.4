package ru.mytheria.main.module.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.mytheria.Mytheria;
import ru.mytheria.api.events.impl.EventTick;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class Unhook extends Module {

    public static boolean ACTIVE = false;

    public Unhook() {
        super(Text.of("Unhook"), null, Category.MISC);
        setKey(-1); // только GUI
    }

    @Override
    public void activate() {
        ACTIVE = true;

        // выключаем все модули кроме Unhook
        Mytheria.getInstance().getModuleManager().forEach(m -> {
            if (m != this && m.getEnabled()) {
                m.setEnabled(false);
            }
        });

        // жёстко закрываем любой экран
        MinecraftClient mc = MinecraftClient.getInstance();
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

    // ⚠️ НЕ override, а EVENT LISTENER
    @Subscribe
    public void onTick(EventTick e) {
        if (!ACTIVE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null) {
            mc.setScreen(null);
        }
    }
}
