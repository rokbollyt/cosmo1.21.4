package ru.mytheria.main.ui.elements.draggable;


import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public class OverlayRenderer {

    public static void rect( DrawContext context, float x, float y, float width, float height) {
        QuickApi.blur()
                .blurRadius(16)
                .radius(new RadiusState(2.5f))
                .size(new SizeState(width, height))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), x, y);

        QuickApi.rectangle()
                .size(new SizeState(width, height))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF00042C, 70), ColorUtil.applyOpacity(0xFF00042C, 70), ColorUtil.applyOpacity(0xFF000537, 70), ColorUtil.applyOpacity(0xFF000537, 70)))
                .radius(new RadiusState(2.5f))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), x, y);
    }

}
