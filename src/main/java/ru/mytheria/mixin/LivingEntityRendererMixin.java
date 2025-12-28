package ru.mytheria.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker; // Импорт для Invoker
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Импорты ваших классов
import ru.mytheria.Mytheria;
import ru.mytheria.api.events.impl.EntityColorEvent;
import ru.mytheria.api.module.Module;
import ru.mytheria.main.module.render.SeeInvisible;

@Mixin(LivingEntityRenderer.class)
// Класс остается абстрактным, чтобы использовать @Invoker
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Unique
    private LivingEntity capturedEntity;

    // --- ИСПРАВЛЕНИЕ 1 & 3: ИСПОЛЬЗУЕМ @Invoker ДЛЯ ВЫЗОВА protected getTexture(T) ---
    // Это связывает наш прокси-метод invokeGetTexture с существующим protected методом getTexture(T entity)
    @Invoker("getTexture")
    protected abstract Identifier invokeGetTexture(S state);
    // -----------------------------------------------------------------------------------

    // --- ЧАСТЬ 1: ЗАХВАТ СУЩНОСТИ ---
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("HEAD")
    )
    private void captureEntity(T livingEntity, S renderState, float tickDelta, CallbackInfo ci) {
        this.capturedEntity = livingEntity;
    }

    // --- ЧАСТЬ 2: ФОРСИРОВАНИЕ ПРОЗРАЧНОСТИ ---
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

            // Используем метод-Invoker
            @SuppressWarnings("unchecked")
            Identifier texture = this.invokeGetTexture(state);

            RenderLayer forcedLayer = RenderLayer.getItemEntityTranslucentCull(texture);

            ci.setReturnValue(forcedLayer);
            ci.cancel();
        }
    }

    // --- ЧАСТЬ 3: МОДИФИКАЦИЯ ЦВЕТА (@ModifyArgs) ---
    @ModifyArgs(
            // Целевой метод - LivingEntityRenderer.render(...)
            method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    // Целевой вызов - EntityModel.render(...)
                    target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"
            )
    )
    // ИСПРАВЛЕНИЕ 2: Сигнатура теперь включает ВСЕ параметры родительского метода render
    private void modifyRenderColor(Args args, S state, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light) {

        // Индекс 4 в EntityModel.render(...) — это int l (Color ARGB)
        int color = args.get(4);

        int modifiedColor = applyColorModification(color, state);

        // Устанавливаем измененный цвет обратно в аргументы
        args.set(4, modifiedColor);
    }

    // --- ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ЛОГИКИ ---
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
            // Вычисляем компонент альфы (сдвиг на 24 бита)
            int alphaComponent = (int)(alpha * 255.0F) << 24;

            // Сохраняем RGB (последние 24 бита) и добавляем новую альфу
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