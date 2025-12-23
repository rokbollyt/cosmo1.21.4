package ru.mytheria.api.events.impl;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.api.events.Event;

public class EventRender3D extends Event {

    @Getter
    public final MatrixStack matrixStack;

    @Getter @Setter
    public float partialTicks;

    @Getter
    public final VertexConsumerProvider.Immediate vertexConsumers;

    @Getter
    public final Camera camera;

    @Getter
    public final Vec3d cameraPos;

    public EventRender3D(MatrixStack matrixStack, float partialTicks,
                         VertexConsumerProvider.Immediate vertexConsumers,
                         Camera camera) {
        this.matrixStack = matrixStack;
        this.partialTicks = partialTicks;
        this.vertexConsumers = vertexConsumers;
        this.camera = camera;
        this.cameraPos = camera.getPos();
    }
}
