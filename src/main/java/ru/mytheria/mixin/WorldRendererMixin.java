package ru.mytheria.mixin;

import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.Mytheria;
import ru.mytheria.api.events.impl.EventRender3D;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(
            ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci
    ) {

        VertexConsumerProvider.Immediate vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        MatrixStack matrices = new MatrixStack();
        matrices.loadIdentity();
        matrices.multiplyPositionMatrix(positionMatrix);

        float tickDelta = tickCounter.getTickDelta(true);

        Mytheria.getInstance().eventProvider.post(
                new EventRender3D(matrices, tickDelta, vertexConsumers, camera)
        );
    }
}
