package ru.mytheria.main.ui.clickGui.components.settings.sliderSetting;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.impl.SliderSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.fonts.main.MsdfUtil;
import ru.mytheria.api.util.shader.common.states.*;
import ru.mytheria.main.ui.clickGui.components.settings.SettingComponent;
import ru.mytheria.api.util.math.Math;
import java.util.Objects;
import java.util.function.Supplier;

public class SliderSettingComponent extends SettingComponent {

    Supplier<String> descriptionText = Suppliers.memoize(() -> MsdfUtil.cutString(getSettingLayer().getDescription().getString(), 6, 240f / 2 - 10));

    public SliderSettingComponent( SliderSetting sliderSetting) {
        super(sliderSetting);
    }

    @Override
    public void init() {
        float moduleNameHeight = QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7);
        float descriptionHeight = QuickApi.inter().getHeight(descriptionText.get(), 6);

        size(240f / 2 - 10, moduleNameHeight + 5 + descriptionHeight + 7.5f);
    }

    @Override
    public SliderSettingComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        SliderSetting sliderSetting = (SliderSetting) getSettingLayer();

        if (sliderSetting.getDragging())
            update(mouseX);

        QuickApi.text()
                .size(8)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 95))
                .text(getSettingLayer().getName().getString())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() - 1);

        if (Objects.nonNull(sliderSetting.getValue())) {
            String valueString = String.format("%.1f", sliderSetting.getValue());
            float valueWidth = 10 + QuickApi.inter().getWidth(valueString, 6);

            QuickApi.border()
                    .size(new SizeState(valueWidth, 9))
                    .radius(new RadiusState(2))
                    .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 25)))
                    .thickness(-1f)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth, getY());

            QuickApi.text()
                    .font(QuickApi.inter())
                    .text(valueString)
                    .color(0xFFFFFFFF)
                    .size(6)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth + 5, getY() + .5f);
        }

        if (!descriptionText.get().isEmpty())
            QuickApi.text()
                .size(6)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 50))
                .text(descriptionText.get())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() + QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7) + 4);

        QuickApi.rectangle()
                .size(new SizeState(getWidth(), 5))
                .radius(new RadiusState(1))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 25)))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() + getHeight() - 5);

        if (Objects.nonNull(sliderSetting.getValue())) {
            float sliderWidth = getWidth() * ((sliderSetting.getValue() - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin()));

            QuickApi.rectangle()
                    .size(new SizeState(sliderWidth, 5))
                    .radius(new RadiusState(1))
                    .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 100)))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() + getHeight() - 5);
        }

        return null;
    }

    void update(double mouseX) {
        SliderSetting sliderSetting = (SliderSetting) getSettingLayer();

        float clampedMouseX = (float) MathHelper.clamp(mouseX, getX(), getX() + getWidth());

        float newValue = sliderSetting.getMin() + ((clampedMouseX - getX()) / (getX() + getWidth() - getX())) * (sliderSetting.getMax() - sliderSetting.getMin());

        newValue = Math.round(newValue / sliderSetting.getIncrements()) * sliderSetting.getIncrements();

        newValue = Math.max(sliderSetting.getMin(), Math.min(sliderSetting.getMax(), newValue));

        sliderSetting.set(newValue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            if (Math.isHover(mouseX, mouseY, getX(), getY() + getHeight() - 5, getWidth(), 5)) {
                SliderSetting sliderSetting = (SliderSetting) getSettingLayer();
                sliderSetting.setDragging(true);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        SliderSetting sliderSetting = (SliderSetting) getSettingLayer();
        sliderSetting.setDragging(false);

        return false;
    }
}
