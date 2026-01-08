package fun.cosmo.mixin;

import net.minecraft.client.render.BackgroundRenderer;
// ... (остальные импорты)
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import fun.cosmo.Mytheria;
import fun.cosmo.api.events.impl.FogEvent;
import fun.cosmo.main.module.render.WorldTweaks; // Импортируем WorldTweaks
import fun.cosmo.api.module.settings.impl.BooleanSetting; // Новый импорт для ясности

@Mixin(BackgroundRenderer.class)
public class BackGroundRendererMixin {

    // Вспомогательный метод для безопасной проверки включения режима "Туман"
    private static boolean isFogModeActive() {
        WorldTweaks module = WorldTweaks.getInstance();

        // 1. Проверка существования модуля
        if (module == null || !module.isEnabled()) {
            return false;
        }

        // 2. Использование ПРАВИЛЬНОГО РУССКОГО КЛЮЧА
        BooleanSetting fogSetting = module.modeSetting.get("Туман");

        // 3. Проверка на null И использование ПРАВИЛЬНОГО МЕТОДА (.getEnabled())
        return fogSetting != null && fogSetting.getEnabled();
    }

    // Инъекция для перехвата цвета тумана
    @Inject(method = "getFogColor", at = @At(value = "HEAD"), cancellable = true)
    private static void mytheria$getFogColorHook(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        if (isFogModeActive()) {
            // Вызываем событие, чтобы WorldTweaks обработал его и установил цвет/дистанцию
            FogEvent event = new FogEvent(0.0F, 0); // Инициализация с заглушками
            Mytheria.getInstance().getEventProvider().post(event);

            // Если WorldTweaks обработал событие и отменил его (установил значения)
            if (event.isCancelled()) {
                int color = event.getColor(); // Получаем цвет, установленный в WorldTweaks

                cir.setReturnValue(new Vector4f(
                        (float) ColorHelper.getRed(color) / 255.0F,
                        (float) ColorHelper.getGreen(color) / 255.0F,
                        (float) ColorHelper.getBlue(color) / 255.0F,
                        (float) ColorHelper.getAlpha(color) / 255.0F
                ));
            }
        }
    }

    @Inject(method = "applyFog", at = @At(value = "HEAD"), cancellable = true)
    private static void mytheria$modifyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f vector4f, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if (isFogModeActive()) {
            // Вызываем событие, чтобы WorldTweaks обработал его и установил цвет/дистанцию
            FogEvent event = new FogEvent(0.0F, 0); // Инициализация с заглушками
            Mytheria.getInstance().getEventProvider().post(event);

            // Если WorldTweaks обработал событие и отменил его (установил значения)
            if (event.isCancelled()) {
                WorldTweaks module = WorldTweaks.getInstance(); // Получаем модуль для доступа к SliderSetting

                float distance = module.distanceSetting.getValue().floatValue(); // Дистанция из WorldTweaks
                int color = event.getColor(); // Цвет, установленный в событии

                cir.setReturnValue(new Fog(
                        2.0F,
                        distance,
                        FogShape.CYLINDER,
                        (float) ColorHelper.getRed(color) / 255.0F,
                        (float) ColorHelper.getGreen(color) / 255.0F,
                        (float) ColorHelper.getBlue(color) / 255.0F,
                        (float) ColorHelper.getAlpha(color) / 255.0F
                ));
                cir.cancel();
            }
        }
    }
}