package fun.cosmo.main.ui.clickGui.components.settings.bindSetting;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import fun.cosmo.api.clientannotation.QuickApi;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.BindSetting;
import fun.cosmo.api.util.color.ColorUtil;
import fun.cosmo.api.util.fonts.main.MsdfUtil;
import fun.cosmo.api.util.keyboard.KeyBoardUtil;
import fun.cosmo.api.util.shader.common.states.ColorState;
import fun.cosmo.api.util.shader.common.states.RadiusState;
import fun.cosmo.api.util.shader.common.states.SizeState;
import fun.cosmo.main.ui.clickGui.components.settings.SettingComponent;
import fun.cosmo.api.util.math.Math;

import java.util.function.Supplier;

public class BindSettingComponent extends SettingComponent {

    Supplier<BindSetting> bindSetting = Suppliers.memoize(() -> (BindSetting) getSettingLayer());
    Supplier<Float> valueWidth = () -> QuickApi.inter().getWidth(KeyBoardUtil.translate(bindSetting.get().getKey()), 6) + 10;
    Supplier<String> descriptionText = Suppliers.memoize(() -> MsdfUtil.cutString(getSettingLayer().getDescription().getString(), 6, 240f / 2 - 10));

    public BindSettingComponent( Setting settingLayer) {
        super(settingLayer);
    }

    @Override
    public void init() {
        float moduleNameHeight = QuickApi.inter().getHeight(getSettingLayer().getName().getString(), 7);
        float descriptionHeight = QuickApi.inter().getHeight(descriptionText.get(), 6);

        size(240f / 2 - 10, moduleNameHeight + 5 + descriptionHeight);
    }
    @Override
    public BindSettingComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animation = getSettingLayer().getAnimation().getOutput().floatValue();

        QuickApi.text()
                .size(7)
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

        QuickApi.border()
                .size(new SizeState(valueWidth.get(), 9))
                .radius(new RadiusState(2))
                .color(new ColorState(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (25 + (25 * animation)))))
                .thickness(-1f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth.get(), getY());

        QuickApi.text()
                .size(6)
                .color(0xFFFFFFFF)
                .text(KeyBoardUtil.translate(bindSetting.get().getKey()))
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - valueWidth.get() + 5, getY() + 1);

        return null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!bindSetting.get().getSelected()) return false;

        bindSetting.get().set(keyCode == GLFW.GLFW_KEY_DELETE ? 0 : keyCode);

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            if (button == 0)
                bindSetting.get().setSelected(!bindSetting.get().getSelected());
            else if (bindSetting.get().getSelected()) {
                bindSetting.get().set(button);
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
