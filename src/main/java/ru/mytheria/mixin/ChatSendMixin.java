package ru.mytheria.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.api.client.ChatCommandExecutor;
import ru.mytheria.main.module.misc.Unhook;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatSendMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {

        // 🔥 ЕСЛИ ВКЛЮЧЕН ANHOOK — НИЧЕГО НЕ ДЕЛАЕМ
        // сообщение улетает на сервер как обычный чат
        if (Unhook.ACTIVE) {
            return;
        }

        // 🔥 ЕСЛИ ANHOOK ВЫКЛЮЧЕН — ОБРАБАТЫВАЕМ КОМАНДЫ
        if (message.startsWith(".")) {
            ChatCommandExecutor.execute(message.substring(1));
            ci.cancel(); // ❌ не отправляем на сервер
        }
    }
}
