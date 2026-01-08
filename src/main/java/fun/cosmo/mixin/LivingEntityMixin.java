package fun.cosmo.mixin;

import fun.cosmo.api.events.EventManager;
import fun.cosmo.api.events.impl.EventFixVelocity;
import fun.cosmo.api.events.impl.EventTravelRotation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
    private void onTravelHead(Vec3d movement, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof ClientPlayerEntity)) return;
        ClientPlayerEntity player = (ClientPlayerEntity) entity;

        // Событие вращения
        EventTravelRotation rotEvent = new EventTravelRotation(player.getYaw(), player.getPitch());
        EventManager.call(rotEvent);
        player.setYaw(rotEvent.getYaw());
        player.setPitch(rotEvent.getPitch());

        // Событие скорости
        Vec3d velocity = player.getVelocity();
        EventFixVelocity velEvent = new EventFixVelocity(velocity.x, velocity.y, velocity.z);
        EventManager.call(velEvent);
        player.setVelocity(velEvent.getX(), velEvent.getY(), velEvent.getZ());
    }
}
