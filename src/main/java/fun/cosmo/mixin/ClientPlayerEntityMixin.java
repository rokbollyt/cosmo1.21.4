package fun.cosmo.mixin;

import fun.cosmo.Mytheria;
import fun.cosmo.main.module.combat.AttackAura;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    private AttackAura getAura() {
        return (AttackAura) Mytheria.getInstance().getModuleManager().find(AttackAura.class);
    }

    private Angle getServerAngle() {
        return RotationController.INSTANCE.getServerAngle();
    }

    // === Ротации в пакетах ===
    @ModifyArgs(method = "sendMovementPackets()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$Full;<init>(DDDFFZZ)V"))
    private void modifyFull(Args args) {
        applyRotations(args, 3, 4);
    }

    @ModifyArgs(method = "sendMovementPackets()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround;<init>(FFZZ)V"))
    private void modifyLook(Args args) {
        applyRotations(args, 0, 1);
    }

    @ModifyArgs(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround;<init>(FFZZ)V"))
    private void modifyVehicle(Args args) {
        applyRotations(args, 0, 1);
    }

    @Redirect(method = "sendMovementPackets()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F", ordinal = 0))
    private float redirectYaw0(ClientPlayerEntity instance) {
        return getRotatedYaw(instance.getYaw());
    }

    @Redirect(method = "sendMovementPackets()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F", ordinal = 1))
    private float redirectYaw1(ClientPlayerEntity instance) {
        return getRotatedYaw(instance.getYaw());
    }

    @Redirect(method = "sendMovementPackets()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F", ordinal = 1))
    private float redirectPitch(ClientPlayerEntity instance) {
        Angle angle = getServerAngle();
        AttackAura aura = getAura();
        if (aura == null || !aura.isEnabled() || angle == null) return instance.getPitch();
        return angle.getPitch();
    }

    private float getRotatedYaw(float original) {
        AttackAura aura = getAura();
        Angle angle = getServerAngle();
        if (aura == null || !aura.isEnabled() || angle == null) return original;
        return angle.getYaw();
    }

    private void applyRotations(Args args, int yawIdx, int pitchIdx) {
        AttackAura aura = getAura();
        Angle angle = getServerAngle();
        if (aura == null || !aura.isEnabled() || angle == null) return;
        args.set(yawIdx, angle.getYaw());
        args.set(pitchIdx, angle.getPitch());
    }
}