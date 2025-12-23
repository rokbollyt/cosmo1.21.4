package ru.mytheria.main.ui.elements.window.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.main.ui.elements.window.InterfaceWindow;

public class BooleanComponent extends InterfaceWindow {

    private final BooleanSetting setting;

    public BooleanComponent( Text name, BooleanSetting setting) {
        super(String.valueOf(name));
        this.setting = setting;
    }

    @Override
    public void render( DrawContext context, int mouseX, int mouseY, float delta ) {
        QuickApi.text()
                .size(8)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, 95))
                .text(setting.getName().getString())
                .font(QuickApi.inter())
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), x, y - 1);
    }

    @Override
    public void mouseClicked( double mouseX, double mouseY, int button ) {
        if (Math.isHover(x + width - 20f, y + 3.5f, 16f, 8f, (float) mouseX, (float) mouseY) && button == 0) setting.set(!setting.getEnabled());
    }

    @Override
    public void mouseReleased( double mouseX, double mouseY, int button ) {

    }

    @Override
    public void keyPressed( int keyCode, int scanCode, int modifiers ) {

    }

    @Override
    public void keyReleased( int keyCode, int scanCode, int modifiers ) {

    }

    @Override
    public void charTyped( char chr, int modifiers ) {

    }
}
