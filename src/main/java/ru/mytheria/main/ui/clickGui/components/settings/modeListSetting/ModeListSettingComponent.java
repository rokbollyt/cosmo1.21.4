package ru.mytheria.main.ui.clickGui.components.settings.modeListSetting;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.DrawContext;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.fonts.main.MsdfUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.api.util.window.WindowLayer;
import ru.mytheria.api.util.window.WindowRepository;
import ru.mytheria.main.ui.clickGui.components.settings.SettingComponent;
import ru.mytheria.main.ui.clickGui.components.settings.modeListSetting.window.ModeListSettingWindowComponent;
import ru.mytheria.main.ui.clickGui.components.settings.modeSetting.ModeSettingComponent;

import java.util.List;
import java.util.function.Supplier;

public class ModeListSettingComponent extends SettingComponent {

    Supplier<ModeListSetting> modeListSetting = Suppliers.memoize(() -> (ModeListSetting) getSettingLayer());

    WindowLayer windowLayer;

    public ModeListSettingComponent(Setting settingLayer) {
        super(settingLayer);

        windowLayer = new ModeListSettingWindowComponent(modeListSetting.get());
    }

    @Override
    public void init() {
        String descriptionText = MsdfUtil.cutString(getSettingLayer().getDescription().getString(), 6, 240f / 2 - 10 - windowLayer.getWidth() - 10);

        windowLayer.init();

        windowLayer.position(getX() + getWidth() - windowLayer.getWidth(), getY() + getHeight() / 2);

        float moduleNameHeight = QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7);
        float descriptionHeight = QuickApi.inter().getHeight(descriptionText, 6);

        size(240f / 2 - 10, moduleNameHeight + 5 + descriptionHeight);
    }


    @Override
    public ModeSettingComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        String descriptionText = MsdfUtil.cutString(getSettingLayer().getDescription().getString(), 6, 240f / 2 - 10 - windowLayer.getWidth() - 10);
        //  System.out.println("ПРОСЫПАЙСЯ: ВТОРОЙ RENDER" + getX() + getY());
        QuickApi.text()
                .size(8)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 95))
                .text(getSettingLayer().getName().getString())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() - 1);

        if (!descriptionText.isEmpty())
            QuickApi.text()
                    .size(6)
                    .color(ColorUtil.applyOpacity(0xFFFFFFFF, 50))
                    .text(descriptionText)
                    .font(QuickApi.inter())
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY() + QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7) + 4);

        String valueText;

        ModeListSetting setting = modeListSetting.get();

        if (setting.empty()) {
            valueText = "N/A";
        } else {
            boolean windowOpen = Mytheria.getInstance().getClickGuiScreen().getWindowRepository().contains(windowLayer);

            if (windowOpen) {
                List<String> selected = setting.getSelected();
                valueText = selected.isEmpty() ? "N/A" : selected.get(0);
            } else {
                int selectedCount = setting.getValues().stream().filter(BooleanSetting::getEnabled).toList().size();
                int total = setting.getValues().size();
                valueText = selectedCount + "/" + total;
            }
        }


        float valueWidth = QuickApi.inter().getWidth(valueText, 6) + 10;

        QuickApi.rectangle()
                .radius(new RadiusState(2))
                .size(new SizeState(valueWidth, 9))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, 10)))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth, getY());

        QuickApi.text()
                .size(6)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 100))
                .text(valueText)
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth + 4.5, getY() + .9);

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        WindowRepository windowRepository = Mytheria.getInstance().getClickGuiScreen().getWindowRepository();

        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()) && !windowRepository.contains(windowLayer)) {
            windowLayer.init(); // на всякий
            windowLayer.position(getX() + getWidth() - windowLayer.getWidth(), getY() + getHeight() / 2);
            windowRepository.push(windowLayer);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


}