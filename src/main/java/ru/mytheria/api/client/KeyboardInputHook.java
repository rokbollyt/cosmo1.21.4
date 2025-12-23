package ru.mytheria.api.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import ru.mytheria.Mytheria;
import ru.mytheria.api.events.impl.KeyEvent;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public final class KeyboardInputHook {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<Integer, Boolean> keyState = new HashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (mc.currentScreen != null) return;

            long handle = mc.getWindow().getHandle();

            for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_LAST; key++) {
                boolean pressed = glfwGetKey(handle, key) == GLFW_PRESS;
                boolean wasPressed = keyState.getOrDefault(key, false);

                // RELEASE -> PRESS (одно событие)
                if (pressed && !wasPressed) {
                    Mytheria.getInstance()
                            .getEventProvider()
                            .post(new KeyEvent(
                                    handle,
                                    key,
                                    0,
                                    GLFW_PRESS,
                                    0
                            ));
                }
                keyState.put(key, pressed);
            }
        });
    }
}
