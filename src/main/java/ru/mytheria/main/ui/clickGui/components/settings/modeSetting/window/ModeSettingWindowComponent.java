package ru.mytheria.main.ui.clickGui.components.settings.modeSetting.window;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.api.util.window.WindowLayer;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.main.ui.clickGui.components.settings.modeSetting.ModeSettingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeSettingWindowComponent extends WindowLayer {

    ModeSetting modeSetting;
    List<Component> components = new ArrayList<>();

    public ModeSettingWindowComponent(ModeSetting modeSetting) {
        this.modeSetting = modeSetting;
        components.addAll(ModeSettingHelper.values(modeSetting));
    }

    @Override
    public void init() {
        size(
            modeSetting.getValues().stream().map(e -> QuickApi.inter().getWidth(e, 8) + 25).reduce(0f, Float::max),
        modeSetting.getValues().size() * 15f
        );
    }

    @Override
    public ModeSettingWindowComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        QuickApi.border()
                .radius(new RadiusState(2))
                .size(new SizeState(getWidth(), getHeight()))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 25)))
                .thickness(-.5f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.blur()
                .radius(new RadiusState(2))
                .size(new SizeState(getWidth(), getHeight()))
                .blurRadius(8)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.rectangle()
                .radius(new RadiusState(2))
                .size(new SizeState(getWidth(), getHeight()))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 65)))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        AtomicReference<Float> offset = new AtomicReference<>(0f);
        components.forEach(e -> {
            e.position(getX(), getY() + offset.get()).size(getWidth(), 15f).render(context, mouseX, mouseY, delta);
            offset.set(offset.get() + 15f);
        });

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            components.forEach(e -> e.mouseClicked(mouseX, mouseY, button));

            return true;
        } else {
            if (getAnimation().getDirection().equals(Direction.BACKWARDS)) return false;

            Mytheria.getInstance().getClickGuiScreen().getWindowRepository().pop(this);
            return true;
        }
    }
}
