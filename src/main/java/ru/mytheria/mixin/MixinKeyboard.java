package ru.mytheria.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.main.module.misc.Unhook;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void blockKeys(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (Unhook.ACTIVE) {
            ci.cancel(); // ❌ НО ТОЛЬКО ПОСЛЕ закрытия GUI
        }
    }
}
