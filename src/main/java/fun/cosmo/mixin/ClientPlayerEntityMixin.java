package fun.cosmo.mixin;

import fun.cosmo.Mytheria;
import fun.cosmo.main.module.combat.AttackAura;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    private AttackAura getAura() {
        return (AttackAura) Mytheria.getInstance().getModuleManager().find(AttackAura.class);
    }

    private Angle getServerAngle() {
        return RotationController.INSTANCE.getServerAngle();
    }

    // --- 1. Full packet ---
    @ModifyArgs(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$Full;<init>(DDDFFZZ)V"
            )
    )
    private void modifyFullPacketAngles(Args args) {
        AttackAura aura = getAura();
        Angle serverAngle = getServerAngle();
        if (aura == null || !aura.isEnabled() || serverAngle == null) return;

        args.set(3, serverAngle.getYaw());
        args.set(4, serverAngle.getPitch());
    }

    // --- 2. Look packet ---
    @ModifyArgs(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$LookAndOnGround;<init>(FFZZ)V"
            )
    )
    private void modifyLookAndOnGroundPacketAngles(Args args) {
        AttackAura aura = getAura();
        Angle serverAngle = getServerAngle();
        if (aura == null || !aura.isEnabled() || serverAngle == null) return;

        args.set(0, serverAngle.getYaw());
        args.set(1, serverAngle.getPitch());
    }

    // --- 3. First yaw for difference ---
    @Redirect(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F",
                    ordinal = 0
            )
    )
    private float redirectFirstYawForDifference(ClientPlayerEntity instance) {
        AttackAura aura = getAura();
        Angle serverAngle = getServerAngle();
        if (aura == null || !aura.isEnabled() || serverAngle == null) {
            return instance.getYaw();
        }
        return serverAngle.getYaw();
    }

    // --- 4. Update lastYaw ---
    @Redirect(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F",
                    ordinal = 1
            )
    )
    private float redirectLastYawUpdate(ClientPlayerEntity instance) {
        AttackAura aura = getAura();
        Angle serverAngle = getServerAngle();
        if (aura == null || !aura.isEnabled() || serverAngle == null) {
            return instance.getYaw();
        }
        return serverAngle.getYaw();
    }

    // --- 5. Update lastPitch ---
    @Redirect(
            method = "sendMovementPackets()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F",
                    ordinal = 1
            )
    )
    private float redirectLastPitchUpdate(ClientPlayerEntity instance) {
        AttackAura aura = getAura();
        Angle serverAngle = getServerAngle();
        if (aura == null || !aura.isEnabled() || serverAngle == null) {
            return instance.getPitch();
        }
        return serverAngle.getPitch();
    }
}