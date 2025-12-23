package ru.mytheria.api.client.draggable.interfaces;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface DraggableApi {

    void render(DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseReleased(double mouseX, double mouseY, int button);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean keyReleased(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char chr, int modifiers);

    void tick();
    
}
