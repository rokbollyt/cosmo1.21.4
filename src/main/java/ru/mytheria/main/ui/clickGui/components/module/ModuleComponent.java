package ru.mytheria.main.ui.clickGui.components.module;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.events.EventManager;
import ru.mytheria.api.events.impl.ModuleEvent;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.keyboard.KeyBoardUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.*;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.main.ui.clickGui.ComponentBuilder;
import ru.mytheria.main.ui.clickGui.Helper;
import ru.mytheria.main.ui.clickGui.components.settings.SettingComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModuleComponent extends Component {

    Module moduleLayer;
    List<SettingComponent> components = new ArrayList<>();

    @NonFinal
    boolean open = false;

    public ModuleComponent(Module moduleLayer) {
        this.moduleLayer = moduleLayer;
        this.components.addAll(Helper.settingComponents(moduleLayer));
    }

    @Override
    public ComponentBuilder render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = moduleLayer.getAnimation().getOutput().floatValue();

        QuickApi.border()
                .size(new SizeState(getWidth(), getHeight()))
                .radius(new RadiusState(5f))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 20)))
                .thickness(-1f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.rectangle()
                .size(new SizeState(getWidth(), getHeight()))
                .radius(new RadiusState(5))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, (int) (10 + (25 * animation)))))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.border()
                .radius(new RadiusState(5))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 40)))
                .thickness(-.5f)
                .size(new SizeState(getWidth(), getHeight()))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());


        String text = moduleLayer.getBinding()
                ? moduleLayer.getKey() != -1
                ? "[" + KeyBoardUtil.translate(moduleLayer.getKey()) + "]"
                : "Press any key"
                : moduleLayer.getModuleName().getString();

        QuickApi.text()
                .size(17f / 2)
                .font(QuickApi.inter())
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (50 + (50 * animation))))
                .text(text)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(),
                        getX() + 5,
                        getY() - 1 + (40f / 2 - QuickApi.inter().getHeight(moduleLayer.getModuleName().getString(), 17f / 2)) / 2);

        QuickApi.text()
                .text("D")
                .size(12)
                .font(QuickApi.icons())
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (100 * animation)))
                .thickness(0.1f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(),
                        getX() + getWidth() - 8 - (10 * animation),
                        getY() + (40f / 2 - 8) / 2 - 2);

        if (open) {
            AtomicReference<Float> offset = new AtomicReference<>(0f);
            components.stream()
                    .filter(e -> e.getSettingLayer().getVisible().get())
                    .forEach(e -> {
                        e.position(getX() + 5f, getY() + 21f + offset.get())
                                .render(context, mouseX, mouseY, delta);
                        offset.set(offset.get() + e.getHeight() + 5f);
                    });
        }

        return null;
    }

    @Override
    public float getHeight() {
        if (!open) return 20f;

        float total = 20f;
        for (SettingComponent setting : components) {
            if (setting.getSettingLayer().getVisible().get()) {
                total += setting.getHeight() + 5f;
            }
        }
        return total;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (moduleLayer.getBinding()) {
            moduleLayer.setKey(button);
            moduleLayer.setBinding(false);
            return true;
        }


        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), 20f)
                && components.stream().noneMatch(e -> e.mouseClicked(mouseX, mouseY, button))) {

            if (button == 0) {
                EventManager.call(new ModuleEvent.ToggleEvent(moduleLayer));
            }

            if (button == 2) {
                moduleLayer.setBinding(!moduleLayer.getBinding());
            }

            if (button == 1) {
                open = !open;
            }

            return true;
        }

        if (open) {
            for (SettingComponent setting : components) {
                if (setting.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (open)
            components.forEach(e -> e.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (moduleLayer.getBinding()) {
            moduleLayer.setKey(keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_ESCAPE ? -1 : keyCode);
            moduleLayer.setBinding(false);
            return true;
        }

        if (open)
            components.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (open)
            components.forEach(e -> e.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
