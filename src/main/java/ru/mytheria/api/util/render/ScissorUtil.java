package ru.mytheria.api.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;

import java.util.Stack;

public class ScissorUtil {

    static Stack<Builder> stack = new Stack<>();

    public static void push(float x, float y, float width, float height) {
        int scale = (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
        int winHeight = MinecraftClient.getInstance().getWindow().getHeight();

        int sx = Math.round(x * scale);
        int sy = winHeight - Math.round((y + height) * scale);
        int sw = Math.round(width * scale);
        int sh = Math.round(height * scale);

        if (!stack.isEmpty()) {
            Builder p = stack.peek();
            int ex = Math.max(sx, p.x);
            int ey = Math.max(sy, p.y);
            int ex2 = Math.min(sx + sw, p.x + p.width);
            int ey2 = Math.min(sy + sh, p.y + p.height);
            sx = ex;
            sy = ey;
            sw = Math.max(0, ex2 - ex);
            sh = Math.max(0, ey2 - ey);
        }

        Builder builder = new Builder().set(sx, sy, sw, sh);
        stack.push(builder);
        builder.apply();
    }


    public static void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
            if (stack.isEmpty()) {
                RenderSystem.disableScissor();
            } else {
                stack.peek().apply();
            }
        }
    }

    private static class Builder {
        int x, y, width, height;

        public Builder set(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            return this;
        }

        public void apply() {
            RenderSystem.enableScissor(x, y, width, height);
        }
    }
}

