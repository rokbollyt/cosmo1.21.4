package fun.cosmo.main.ui.clickGui.components.settings.modeListSetting.window;


import net.minecraft.client.gui.DrawContext;
import fun.cosmo.Mytheria;
import fun.cosmo.api.clientannotation.QuickApi;
import fun.cosmo.api.module.settings.impl.ModeListSetting;
import fun.cosmo.api.util.animations.Direction;
import fun.cosmo.api.util.color.ColorUtil;
import fun.cosmo.api.util.math.Math;
import fun.cosmo.api.util.shader.common.states.ColorState;
import fun.cosmo.api.util.shader.common.states.RadiusState;
import fun.cosmo.api.util.shader.common.states.SizeState;
import fun.cosmo.api.util.window.WindowLayer;
import fun.cosmo.main.ui.clickGui.Component;
import fun.cosmo.main.ui.clickGui.components.settings.modeListSetting.ModeListSettingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ModeListSettingWindowComponent extends WindowLayer {

    ModeListSetting modeListSetting;
    List<Component> components = new ArrayList<>();

    public ModeListSettingWindowComponent(ModeListSetting modeListSetting) {
        this.modeListSetting = modeListSetting;
        components.addAll(ModeListSettingHelper.values(modeListSetting));
    }

    @Override
    public void init() {
        size(
                modeListSetting.asStringList().stream().map(e -> QuickApi.inter().getWidth(e, 8) + 25).reduce(0f, Float::max),
                modeListSetting.asStringList().size() * 15f
        );
    }

    @Override
    public ModeListSettingWindowComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
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
