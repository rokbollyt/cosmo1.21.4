package ru.mytheria.main.ui.clickGui.components.settings.booleanSetting;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.fonts.main.MsdfUtil;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.components.settings.SettingComponent;
import ru.mytheria.api.util.math.Math;

import java.awt.*;
import java.util.function.Supplier;

public class BooleanSettingComponent extends SettingComponent {

    Supplier<String> descriptionText = Suppliers.memoize(() -> MsdfUtil.cutString(getSettingLayer().getDescription().getString(), 6, 240f / 2 - 35));

    public BooleanSettingComponent(Setting settingLayer) {
        super(settingLayer);
    }

    @Override
    public void init() {
        float moduleNameHeight = QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7);
        float descriptionHeight = QuickApi.inter().getHeight(descriptionText.get(), 6);

        size(240f / 2 - 10, moduleNameHeight + 5 + descriptionHeight);
    }

    @Override
    public BooleanSettingComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = getSettingLayer().getAnimation().getOutput().floatValue();

        BooleanSetting booleanSetting = (BooleanSetting) getSettingLayer();

        Color bgColor = booleanSetting.getEnabled()
                ? new Color(0, 122, 204, 255)
                : new Color(96, 96, 96, 100);

        QuickApi.rectangle()
                .size(new SizeState(18, 10))
                .radius(new RadiusState(4f))
                .color(new ColorState(bgColor))
                .build()
                .render(getX() + getWidth() - 8 - 10,
                        getY() + (getHeight() - 8) / 2 - 2);

        float animationValue = getSettingLayer().getAnimation().getOutput().floatValue();
        float switchX = getX() + getWidth() - 8 - 9;
        float baseX = switchX + 1.5f;
        float travelDistance = 16 - 7 - (1.5f * 2);
        float trackX = baseX + (animation * travelDistance);

        QuickApi.rectangle()
                .size(new SizeState(7, 7))
                .radius(new RadiusState(2f))
                .color(new ColorState(new Color(255, 255, 255, 255)))
                .build()
                .render(trackX,
                        getY() + (getHeight() - 8) / 2 - 0.5);


        QuickApi.text()
                .size(8)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 95))
                .text(getSettingLayer().getName().getString())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() - 1);

        if (!descriptionText.get().isEmpty())
            QuickApi.text()
                .size(6)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 50))
                .text(descriptionText.get())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() + QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7) + 4);

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            BooleanSetting booleanSetting = (BooleanSetting) getSettingLayer();
            booleanSetting.set(!booleanSetting.getEnabled());

            return true;
        }

        return false;
    }
}
