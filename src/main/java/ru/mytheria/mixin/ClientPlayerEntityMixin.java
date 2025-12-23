package ru.mytheria.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.Mytheria;
import ru.mytheria.api.events.impl.EventPlayerTick;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick( CallbackInfo ci) {
        EventPlayerTick event = new EventPlayerTick();
        Mytheria.getEventProvider().post(event);
    }
}
