package ru.mytheria.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.api.events.EventManager;
import ru.mytheria.api.events.impl.KeyEvent;
import ru.mytheria.main.ui.clickGui.ClickGuiScreen;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

@Mixin(Keyboard.class)
public class KeyBoardMixin {

    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/InactivityFpsLimiter;onInput()V"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {

        if (mc.world == null || mc.player == null) return;

        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            EventManager.call(new KeyEvent(window, key, scancode, action, modifiers));
        }
    }
}
