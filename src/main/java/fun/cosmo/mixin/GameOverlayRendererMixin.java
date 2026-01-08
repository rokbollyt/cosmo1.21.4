package fun.cosmo.mixin;

import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.cosmo.main.module.render.NoRender;

@Mixin(InGameOverlayRenderer.class)
public class GameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void removeFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        // ✅ ИСПРАВЛЕНИЕ: Безопасная проверка на null
        if (NoRender.INSTANCE != null && NoRender.INSTANCE.isRemoveFire()) {
            ci.cancel();
        }
    }
}