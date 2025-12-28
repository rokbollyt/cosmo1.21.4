package ru.mytheria.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

// Событие рендера мира (аналог WorldRenderEvent/EventRender3D)
@AllArgsConstructor
@Getter
public class WorldRenderEvent {
    private final MatrixStack stack;
    private final float partialTicks;
}