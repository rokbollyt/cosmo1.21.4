package ru.mytheria.api.client.draggable.data;


import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.client.draggable.Draggable;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.MathCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.elements.Watermark;
import ru.mytheria.main.ui.elements.draggable.KeybindsDraggable;
import ru.mytheria.main.ui.elements.draggable.TargetDraggable;

import static net.minecraft.client.util.InputUtil.isKeyPressed;
import static ru.mytheria.api.util.math.Math.stick;

public class DraggableRepository implements QuickImport {

    List<Draggable> list = new ArrayList<>();

    MathCounter controlClickTime = MathCounter.create();
    Supplier<Boolean> altPressed = () -> isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT);
    Boolean netEnabled = true;

    public void init() {
        list.addAll(List.of(
                new Watermark(),
                new KeybindsDraggable(),
                new TargetDraggable()
        ));
    }

    public void render(DrawContext context, RenderTickCounter tickCounter, double mouseX, double mouseY) {
        list.forEach(e -> {
            if (e.getVisible().get() || mc.currentScreen instanceof ChatScreen) {
                e.getAnimation().setDirection(Direction.FORWARDS);
            } else {
                e.getAnimation().setDirection(Direction.BACKWARDS);
            }

            if (!e.getVisible().get() && e.getAnimation().isFinished(Direction.BACKWARDS) && !(mc.currentScreen instanceof ChatScreen)) return;

            Math.scale(context.getMatrices(), e.getX() + e.getWidth() / 2, e.getY() + e.getHeight() / 2, e.getAnimation().getOutput().floatValue(), () -> {
                e.render(context, mouseX, mouseY, tickCounter);

                if (mc.currentScreen instanceof ChatScreen) {
                    //renderText(context, "X: " + String.format("%.2f", e.getX()), e.getX() + (e.getWidth() / 2), e.getY() - 13, 255);
                    //renderText(context, "Y: " + String.format("%.2f", e.getY()), e.getX() + (e.getWidth() / 2), e.getY() - 5, 255);
                }
            });
        });
    }

    public void update(DrawContext context, float tickDelta, int mouseX, int mouseY) {
        if (netEnabled) {
            renderNet(context.getMatrices().peek().getPositionMatrix());
        }

        list.stream()
                .filter(Draggable::getDragging)
                .findFirst()
                .ifPresent(e -> {
                    float screenWidth = mc.getWindow().getScaledWidth();
                    float screenHeight = mc.getWindow().getScaledHeight();

                    float nearestX = Math.round((e.getX() + (e.getWidth() / 2)) / (screenWidth / 10f)) * (screenWidth / 10f);
                    float nearestY = Math.round((e.getY() + (e.getHeight() / 2)) / (screenHeight / 10f)) * (screenHeight / 10f);

                    float РезкаяpedX = Math.clamp(stick(mouseX - (e.getWidth() / 2), nearestX - (e.getWidth() / 2), 10f), 0, screenWidth - e.getWidth());
                    float РезкаяpedY = Math.clamp(stick(mouseY - (e.getHeight() / 2), nearestY - (e.getHeight() / 2), 10f), 0, screenHeight - e.getHeight());

                    e.setX(altPressed.get() ? e.getX() : netEnabled ? MathHelper.lerp(.1f, e.getX(), РезкаяpedX) : mouseX - (e.getWidth() / 2));
                    e.setY(netEnabled ? MathHelper.lerp(.1f, e.getY(), РезкаяpedY) : mouseY - (e.getHeight() / 2));

                    renderLine(context.getMatrices().peek().getPositionMatrix(), 0, e.getY() - .75f + (e.getHeight() / 2), mc.getWindow().getScaledWidth(), 1.5f, 255f);
                    renderLine(context.getMatrices().peek().getPositionMatrix(), e.getX() - .75f + (e.getWidth() / 2), 0, 1.5f, mc.getWindow().getScaledHeight(), 255f);
        });
    }

    void renderNet(Matrix4f matrix4f) {
        float xOffset = 0;
        float yOffset = 0;

        QuickApi.blur()
                .blurRadius(16)
                .size(new SizeState(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()))
                .color(new ColorState(0xFFFFFFFF))
                .build()
                .render(matrix4f,0,0);

        QuickApi.rectangle()
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 127.5f)))
                .size(new SizeState(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()))
                .build()
                .render(matrix4f, 0, 0);

        for (int i = 0; i < mc.getWindow().getScaledWidth() / 10 ; i++) {
            renderLine(matrix4f, xOffset - .75f, 0, 1.5f, mc.getWindow().getScaledHeight(), 85);

            xOffset += (float) mc.getWindow().getScaledWidth() / 10;
        }

        for (int j = 0; j < mc.getWindow().getScaledHeight() / 10; j++) {
            renderLine(matrix4f, 0, yOffset - .75f, mc.getWindow().getScaledWidth(), 1.5f, 85);

            yOffset += (float) mc.getWindow().getScaledHeight() / 10;
        }
    }

    void renderLine(Matrix4f matrix4f, float x, float y, float width, float height, float opacity) {
        QuickApi.rectangle()
                .size(new SizeState(width, height))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, opacity)))
                .build()
                .render(matrix4f, x, y);
    }

    void renderText(DrawContext context, String text, float x, float y, float opacity) {
        QuickApi.text()
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, opacity))
                .text(text)
                .size(8)
                .font(QuickApi.inter())
                .outline(0xFF000000, .2f)
                .thickness(0.1f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), x - 2f - QuickApi.inter().getWidth(text, 8) / 2, y - QuickApi.inter().getHeight(text, 8));
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        list.forEach(e -> {
            if (!e.getVisible().get() && e.getAnimation().isFinished(Direction.BACKWARDS) && !(mc.currentScreen instanceof ChatScreen)) return;

            if (Math.isHover(mouseX, mouseY, e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
                if (button == 0 && e.isDraggable()) {
                    if (!e.getDragging()) {
                        e.setDragging(true);
                        e.position((float) (mouseX - e.getWidth() / 2), (float) (mouseY - e.getHeight() / 2));
                    }
                }


                if (button == 1) {
                    e.toggleSetting();
                }
            }
        });
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        list.forEach(e -> {
            if (!e.getVisible().get() && !(mc.currentScreen instanceof ChatScreen)) return;

            if (button == 0) {
                e.setDragging(false);
            }
        });
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) && keyCode == GLFW.GLFW_KEY_LEFT_ALT) {
            if (!controlClickTime.isReached(30)) return false;

            if (list.stream()
                    .filter(e -> e.getVisible().get() && e.keyPressed(keyCode, scanCode, modifiers))
                    .noneMatch(e -> e.keyPressed(keyCode, scanCode, modifiers))) {

                netEnabled = !netEnabled;
                controlClickTime.resetCounter();

                return true;
            }
        }

        return false;
    }
}