package ru.mytheria.main.ui.clickGui.components.settings.modeSetting;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeSettingValueComponent extends Component {

    ModeSetting setting;
    String value;

    @Override
    public ModeSettingValueComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        float animationValue = setting.getAnimation().getOutput().floatValue();

        QuickApi.rectangle()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, setting.equals(value) ? (int) (60 * animationValue) : 0)))
                .radius(new RadiusState(2))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.text()
                .font(QuickApi.inter())
                .size(8)
                .text(value)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (setting.equals(value) ? 50 + (50 * animationValue) : 50)))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 5, getY() + QuickApi.inter().getHeight(value, 8) / 4);

        QuickApi.text()
                .text("B")
                .size(7)
                .font(QuickApi.icons())
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, setting.equals(value) ? (int) (100 * animationValue) : 0))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - 7 - 5, getY() - .5f + (getHeight() - 7) / 2);

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.set(value);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
