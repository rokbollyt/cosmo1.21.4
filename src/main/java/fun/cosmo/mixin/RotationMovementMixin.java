package fun.cosmo.mixin;

import fun.cosmo.Mytheria;
import fun.cosmo.main.module.combat.AttackAura;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.rotation.RotationController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class RotationMovementMixin {

    @Shadow private float lastYaw;
    @Shadow private float lastPitch;
    @Shadow @Final protected MinecraftClient client;
    @Shadow public Input input;

    // Хранилища для восстановления инпутов
    private float originalForward = 0;
    private float originalStrafe = 0;

    // Хранилища для углов (только для пакетов)
    private float originalLocalYaw = Float.NaN;
    private float originalLocalPitch = Float.NaN;

    private Angle getTargetRotation() {
        AttackAura aura = (AttackAura) Mytheria.getInstance().getModuleManager().find(AttackAura.class);
        if (aura == null || !aura.isEnabled()) {
            return null;
        }
        return RotationController.INSTANCE.getServerAngle();
    }

    /**
     * ЧАСТЬ 1: MOVEMENT FIX (Только математика инпутов)
     * Вращает WASD-инпуты, чтобы вектор движения совпадал с углом сервера.
     * НЕ ТРОГАЕТ player.setYaw/setPitch.
     */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovementPre(CallbackInfo ci) {
        Angle serverAngle = this.getTargetRotation();
        if (serverAngle == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        float realYaw = player.getYaw();

        // 1. Сохраняем оригинальные нажатия клавиш
        this.originalForward = this.input.movementForward;
        this.originalStrafe = this.input.movementSideways;

        // Если игрок стоит на месте, rotation spoof не нужен
        if (this.originalForward == 0 && this.originalStrafe == 0) return;

        // 2. Вычисляем разницу между углом сервера и локальным углом
        float yawDiff = serverAngle.getYaw() - realYaw;

        // ****** КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ ДЛЯ GRIM И СБРОСА ******
        // Если разница углов меньше 0.1 градуса, мы считаем, что идет сброс
        // или цель не выбрана, и отключаем Input Rotation, чтобы избежать float point ошибок.
        // ЭТО ЧИНИТ ФЛАГИ "без таргета с включенной килкой".
        if (Math.abs(yawDiff) < 0.1f) {
            return;
        }
        // *******************************************************

        // 3. Поворачиваем вектор движения (Silent Move)
        float forward = this.originalForward;
        float strafe = this.originalStrafe;

        float f = MathHelper.sin(yawDiff * 0.017453292F);
        float g = MathHelper.cos(yawDiff * 0.017453292F);

        // Подменяем ТОЛЬКО инпуты. Yaw игрока остается прежним!
        this.input.movementSideways = strafe * g - forward * f;
        this.input.movementForward = forward * g + strafe * f;
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void onTickMovementPost(CallbackInfo ci) {
        // Восстанавливаем инпуты, если они были сохранены
        if (this.originalForward != 0 || this.originalStrafe != 0) {
            this.input.movementForward = this.originalForward;
            this.input.movementSideways = this.originalStrafe;

            // Сброс, чтобы не влиять на следующий тик, если return был в onTickMovementPre
            this.originalForward = 0;
            this.originalStrafe = 0;
        }
    }

    /**
     * ЧАСТЬ 2: PACKET SPOOF
     * Подмена угла только на момент отправки пакета.
     */
    @Inject(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z", shift = At.Shift.AFTER))
    private void onSendMovementPacketsPre(CallbackInfo ci) {
        Angle serverAngle = this.getTargetRotation();

        // Если аура выключена ИЛИ находится в фазе сброса (где angle == null), выходим.
        // Если angle != null, но yawDiff < 0.1f, пакет отправляется, но уже с углами, близкими к локальным.
        if (serverAngle == null) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Сохраняем реальные углы
        this.originalLocalYaw = player.getYaw();
        this.originalLocalPitch = player.getPitch();

        // Ставим серверные углы только для конструктора пакета
        player.setYaw(serverAngle.getYaw());
        player.setPitch(serverAngle.getPitch());

        // Форсируем отправку пакета поворота
        this.lastYaw = MathHelper.wrapDegrees(serverAngle.getYaw() - 1.0F);
        this.lastPitch = MathHelper.wrapDegrees(serverAngle.getPitch() - 1.0F);
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"))
    private void onSendMovementPacketsPost(CallbackInfo ci) {
        if (Float.isNaN(this.originalLocalYaw)) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Мгновенно возвращаем углы назад
        player.setYaw(this.originalLocalYaw);
        player.setPitch(this.originalLocalPitch);

        this.originalLocalYaw = Float.NaN;
        this.originalLocalPitch = Float.NaN;
    }
}