package ru.mytheria.main.ui.clickGui;

import lombok.Getter;
import ru.mytheria.api.clientannotation.QuickImport;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Component implements ComponentBuilder, QuickImport {

    final List<Component> componentList = new ArrayList<>();

    float x, y, width, height;

    @Override
    public Component position(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    @Override
    public Component size(float width, float height) {
        this.width = width;
        this.height = height;

        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public void init() {

    }
}
