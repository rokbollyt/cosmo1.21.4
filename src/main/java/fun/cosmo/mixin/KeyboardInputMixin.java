package fun.cosmo.mixin;

import fun.cosmo.Mytheria;
import fun.cosmo.main.module.combat.AttackAura;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        AttackAura aura = (AttackAura) Mytheria.getInstance().getModuleManager().find(AttackAura.class);
        if (aura == null || !aura.isEnabled()) return;

        Angle serverAngle = RotationController.INSTANCE.getServerAngle();
        if (serverAngle == null) return;

        if (!RotationController.INSTANCE.isMoveCorrection()) return;

        KeyboardInput input = (KeyboardInput)(Object)this;

        float clientYaw = mc.player.getYaw();
        float targetYaw = serverAngle.getYaw();

        float deltaYaw = MathHelper.wrapDegrees(targetYaw - clientYaw);
        float radians = (float) Math.toRadians(deltaYaw);

        float cos = MathHelper.cos(radians);
        float sin = MathHelper.sin(radians);

        float forward = input.movementForward;
        float sideways = input.movementSideways;

        // Стандартная формула поворота (проходит Grim на 1.21.4)
        float newForward = forward * cos + sideways * sin;
        float newSideways = sideways * cos - forward * sin;

        input.movementForward = newForward;
        input.movementSideways = newSideways;

        // В 1.21+ булевые флаги pressingForward и т.д. УДАЛЕНЫ из класса Input!
        // Они больше не существуют — ваниль теперь использует только movementForward/Sideways напрямую.
        // Нет нужды их пересчитывать и нет диагональной коррекции через *= 0.98F (в новых версиях её нет).

        // Поэтому просто оставляем новые значения — это всё, что нужно для идеального move fix.
    }
}