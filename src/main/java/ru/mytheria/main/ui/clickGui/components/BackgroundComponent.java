package ru.mytheria.main.ui.clickGui.components;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.render.RenderEngine;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.api.util.math.Math;

import java.awt.*;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BackgroundComponent extends Component {

    Category category;

    Animation animation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    @Override
    public BackgroundComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()))
            animation.setDirection(Direction.FORWARDS);
        else animation.setDirection(Direction.BACKWARDS);


        QuickApi.blur()
                .size(new SizeState(getWidth(), getHeight()))
                .radius(new RadiusState(10))
                .blurRadius(16)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.rectangle()
                .radius(new RadiusState(10))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 40)))
                .size(new SizeState(getWidth(), getHeight()))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.border()
                .radius(new RadiusState(10))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 40)))
                .thickness(-.5f)
                .size(new SizeState(getWidth(), getHeight()))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());


                QuickApi.text()
                .size(10)
                .font(QuickApi.inter())
                .text(category.name())
                .color(0xFFFFFFFF)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 20, getY() + 10);


        List<String> modulesList = Mytheria.getInstance().getModuleManager().getModuleLayers().stream()
                .filter(e -> e.getCategory().equals(category))
                .limit(3)
                .map(e -> e.getModuleName().getString())
                .toList();


        return null;
    }
}
