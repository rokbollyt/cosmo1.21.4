package fun.cosmo.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Импорты ваших классов
import fun.cosmo.Mytheria;
import fun.cosmo.api.events.impl.EntityColorEvent;
import fun.cosmo.api.module.Module;
import fun.cosmo.main.module.render.SeeInvisible;
import fun.cosmo.main.module.render.HitColor;
import fun.cosmo.api.util.color.ColorUtil;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Unique
    private LivingEntity capturedEntity;

    @Invoker("getTexture")
    protected abstract Identifier invokeGetTexture(S state);

    // --- ЧАСТЬ 1: ЗАХВАТ СУЩНОСТИ ---
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("HEAD")
    )
    private void captureEntity(T livingEntity, S renderState, float tickDelta, CallbackInfo ci) {
        this.capturedEntity = livingEntity;
    }

    // --- ЧАСТЬ 2: ФОРСИРОВАНИЕ ПРОЗРАЧНОСТИ (SeeInvisible) ---
    @Inject(
            method = "getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void forceTranslucentLayer(S state, boolean bl, boolean bl2, boolean bl3,
                                       CallbackInfoReturnable<RenderLayer> ci) {
        if (this.capturedEntity == null) return;

        Module module = Mytheria.getInstance().getModuleManager().getModule("SeeInvisible");

        if (state.invisibleToPlayer &&
                module instanceof SeeInvisible &&
                module.isEnabled()) {

            @SuppressWarnings("unchecked")
            Identifier texture = this.invokeGetTexture(state);

            RenderLayer forcedLayer = RenderLayer.getItemEntityTranslucentCull(texture);

            ci.setReturnValue(forcedLayer);
            ci.cancel();
        }
    }

    // 🔥 ОБНОВЛЕННАЯ ЛОГИКА HITCOLOR В getMixColor
    @Overwrite
    public int getMixColor(S state) {
        HitColor module = HitColor.getInstance();

        if (module != null && module.isActive() && state.hurt) {

            // 1. Получаем базовый клиентский цвет (RGB с A=255)
            int baseColor = ColorUtil.getClientColor();

            // 2. Получаем желаемую альфа-прозрачность из настройки модуля
            float alphaValue = module.getAlphaValue();

            // 3. Конвертируем float [0.0, 1.0] в int [0, 255]
            int newAlpha = (int) (alphaValue * 255.0F);

            // 4. Применяем новую альфу к базовому цвету
            // ColorUtil.changeAlpha возвращает НОВЫЙ ARGB цвет,
            // где альфа-канал заменен на newAlpha.
            return ColorUtil.changeAlpha(baseColor, newAlpha);
        }

        return -1;
    }

    // --- ЧАСТЬ 3: МОДИФИКАЦИЯ ЦВЕТА (@ModifyArgs) ---
    // Эта часть будет продолжать работать для SeeInvisible,
    // поскольку color (args.get(4)) теперь содержит цвет, установленный getMixColor (HitColor),
    // и SeeInvisible просто модифицирует его Альфа-канал.
    @ModifyArgs(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"
            )
    )
    private void modifyRenderColor(Args args, S state, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light) {

        int color = args.get(4);
        int modifiedColor = applyColorModification(color, state);
        args.set(4, modifiedColor);
    }

    // --- ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ЛОГИКИ SEEINVISIBLE ---
    @Unique
    private int applyColorModification(int color, S state) {
        if (capturedEntity == null) {
            return color;
        }

        Mytheria mytheria = Mytheria.getInstance();
        Module module = mytheria.getModuleManager().getModule("SeeInvisible");

        if (state.invisibleToPlayer &&
                module instanceof SeeInvisible seeInvisible &&
                module.isEnabled()) {

            float alpha = seeInvisible.getAlphaValue();
            int alphaComponent = (int)(alpha * 255.0F) << 24;

            // Сохраняем RGB (который может быть HitColor) и добавляем новую альфу (SeeInvisible)
            int rgb = color & 0x00FFFFFF;
            int finalColor = rgb | alphaComponent;

            EntityColorEvent event = new EntityColorEvent(finalColor, capturedEntity);
            mytheria.getEventProvider().post(event);

            return event.getColor();
        }

        return color;
    }

    // --- ЧАСТЬ 4: СБРОС СУЩНОСТИ ---
    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN")
    )
    private void resetCapturedEntity(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     int light, CallbackInfo ci) {
        this.capturedEntity = null;
    }
}