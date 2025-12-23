package ru.mytheria.api.client.configuration;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class AutoConfigTask {

    private static final ConfigurationService CONFIG = new ConfigurationService();
    private static int ticks;

    private static final int SAVE_INTERVAL = 20 * 60 * 5; // 5 минут

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks++;
            if (ticks >= SAVE_INTERVAL) {
                CONFIG.save("autosave");
                ticks = 0;
            }
        });
    }
}
