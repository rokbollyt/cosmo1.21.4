package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class RenderEvent extends Event {

    @Getter
    @AllArgsConstructor
    public static class AfterHand extends RenderEvent {
        MatrixStack stack;
        RenderTickCounter tickCounter;
    }

    @Getter
    @AllArgsConstructor
    public static class BeforeHud extends RenderEvent {
        DrawContext context;
        RenderTickCounter tickCounter;
    }

    @Getter
    @AllArgsConstructor
    public static class AfterHud extends RenderEvent {
        DrawContext context;
        RenderTickCounter tickCounter;
    }

    @Getter
    @AllArgsConstructor
    public static class AfterChat extends RenderEvent {
        DrawContext context;
        int mouseX;
        int mouseY;
        float delta;
    }

    @Getter
    @AllArgsConstructor
    public static class RenderLabelsEvent<T extends Entity, S extends EntityRenderState> extends RenderEvent {
        S state;
    }

}
