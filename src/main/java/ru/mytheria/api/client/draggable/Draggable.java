package ru.mytheria.api.client.draggable;

import ru.mytheria.api.clientannotation.QuickApi;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import ru.mytheria.api.client.draggable.interfaces.DraggableApi;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Draggable implements DraggableApi, QuickApi {

    @Setter
    @NonNull
    Float x, y, width, height;

    @Setter
    Float prevX, prevY;

    @Setter
    @NonNull
    Supplier<Boolean> visible;

    @Setter
    @NonFinal
    Boolean dragging = false;

    @NonFinal
    Boolean settingOpened = false;

    final List<Setting> settings = new ArrayList<>();

    final Animation animation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    final Animation settingAnimation = new DecelerateAnimation()
            .setMs(300)
            .setValue(1);

    public void toggleSetting() {
        this.settingOpened = !this.settingOpened;
        this.settingAnimation.setDirection(this.settingOpened ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public void position(float x, float y) {
        this.prevX = this.x;
        this.prevY = this.y;
        this.x = x;
        this.y = y;
    }

    public boolean isDraggable() {
        return true;
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
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    @Override
    public void tick() {}
}
