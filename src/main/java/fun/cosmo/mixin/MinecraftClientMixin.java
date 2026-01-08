package fun.cosmo.mixin;

import fun.cosmo.api.events.EventManager;
import fun.cosmo.api.events.impl.EventTick;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void hookClientTick(CallbackInfo ci) {
        EventManager.call(new EventTick());
    }
}
