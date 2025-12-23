package ru.mytheria.api.util.window;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;
import ru.mytheria.main.ui.clickGui.ComponentBuilder;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class WindowLayer implements ComponentBuilder {

    float x,y, width, height;
    Animation animation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    @Override
    public WindowLayer position(float x, float y) {
        this.x = x;
        this.y = y;

        return this;
    }

    @Override
    public WindowLayer size(float width, float height) {
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
