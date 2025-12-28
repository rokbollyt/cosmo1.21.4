package ru.mytheria.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(
            method = "update",
            at = @At(value = "STORE", ordinal = 0),
            name = "f"
    )
    private float modifyCameraShakeY(float original) {
        // Добавляем проверку на null перед вызовом методов
        try {
            ru.mytheria.main.module.render.NoRender instance = ru.mytheria.main.module.render.NoRender.INSTANCE;
            if (instance != null && instance.isEnabled() && instance.isRemoveShake()) {
                return 0.0F;
            }
        } catch (Exception e) {
            // Игнорируем ошибки, если что-то пошло не так
        }
        return original;
    }
}