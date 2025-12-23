package ru.mytheria.main.ui.clickGui.components.settings.modeListSetting;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModeListSettingValueComponent extends Component {

    ModeListSetting setting;
    String value;

    @Override
    public ModeListSettingValueComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
    //    System.out.println("ПРОСЫПАЙСЯ: ТРЕТИЙ RENDER" + getX() + getY());
        QuickApi.rectangle()
                .size(new SizeState(getWidth(), getHeight()))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, (int) (60 * setting.get(value).getAnimation().getOutput().floatValue()))))
                .radius(new RadiusState(2))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.text()
                .font(QuickApi.inter())
                .size(8)
                .text(value)
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (50 + (50 * setting.get(value).getAnimation().getOutput().floatValue()))))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 5, getY() + QuickApi.inter().getHeight(value, 8) / 4);

     /*   QuickApi.text()
                .text("B")
                .size(7)
                .font(QuickApi.icons())
                .color(ColorUtil.applyOpacity(0xFFFFFFFF, (int) (100 * setting.get(value).getAnimation().getOutput().floatValue())))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() - 7 - 5, getY() - .5f + (getHeight() - 7) / 2);
*/
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.get(value).set(!setting.get(value).getEnabled());
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
