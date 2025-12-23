package ru.mytheria.main.ui.elements.draggable;


import com.google.common.base.Suppliers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.client.draggable.Draggable;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.keyboard.KeyBoardUtil;
import ru.mytheria.api.util.render.RenderEngine;
import ru.mytheria.api.util.render.ScissorUtil;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.module.render.Interface;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static ru.mytheria.main.ui.elements.draggable.OverlayRenderer.rect;

public class KeybindsDraggable extends Draggable {

    static Supplier<List<Module>> modules = () -> Mytheria.getInstance().getModuleManager().getModuleLayers().stream()
            .filter(e -> e.getAnimation().getOutput().floatValue() > 0 && !e.getKey().equals(-1))
            .toList();

    static Supplier<Interface> module = Suppliers.memoize(() -> (Interface) Mytheria.getInstance().getModuleManager().find(Interface.class));

    public KeybindsDraggable() {
        super(10f, 25f, 60f, 15f, () -> module.get().getEnabled() && module.get().getVisible().get("Keybinds").getEnabled() && !modules.get().isEmpty());
    }

    @Override
    public void render( DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter) {
        //  rect(context, getX(), getY(), getWidth(), getHeight());
        String mode = module.get().getMode().getValue();

        if (mode.equals("Стекло")) {
        QuickApi.blur()
                .size(new SizeState(getWidth() + 15, getHeight() + 11))
                .radius(new RadiusState(7))
                .blurRadius(10)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        RenderEngine.drawLiquid(context.getMatrices(),
                getX(), getY(), getWidth() + 15, getHeight() + 11,
                new ColorState(new Color(200, 200, 200)),
                new RadiusState(7),
                1f,   // smoothness
                9.0f,  // glassDirection
                25.0f,  // glassQuality
                1.2f    // glassSize
        );

        RenderEngine.drawRectangle(context.getMatrices(),
                getX(),
                getY() + 14.5F,
                getWidth() + 15F,
                getHeight() - 17,
                new ColorState(new Color(59, 59, 59, 65)),
                new RadiusState(0),
                1.0f,
                75f);
    }
    if (mode.equals("Блюр")) {

        QuickApi.border()
                .radius(new RadiusState(7))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 40)))
                .thickness(-.5f)
                .size(new SizeState(getWidth() + 15, getHeight() + 13))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

            RenderEngine.drawBlurRectangle(context.getMatrices(),
                    getX(), getY(), getWidth() + 15, getHeight() + 13,
                    new ColorState(new Color(0xFF464646, true)),
                    new RadiusState(7),
                    1f,
                    55,
                    0.1f);

        RenderEngine.drawRectangle(context.getMatrices(),
                getX(),
                getY() + 14.5f,
                getWidth() + 15F,
                getHeight() - 17,
                new ColorState(new Color(35, 35, 35, 92)),
                new RadiusState(0),
                1.0f,
                75f);

    }

        QuickApi.text()
                .font(QuickApi.sf_semi())
                .text("Клавиши")
                .color(new Color(200, 200, 200))
                .size(8.5f)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 5.5f, getY() - 1 + (15 - QuickApi.inter().getHeight("Keybinds", 7)) / 2);

        AtomicReference<Float> height = new AtomicReference<>(0f);
        modules.get().forEach(e -> height.set(height.get() + 6 * e.getAnimation().getOutput().floatValue()));

        setHeight(15f + height.get());

        AtomicReference<Float> offset = new AtomicReference<>(1f);

        ScissorUtil.push(getX(), getY(), getWidth() + 15, getHeight() + 15);
        modules.get().forEach(e -> {
            QuickApi.text()
                    .size(7)
                    .font(QuickApi.sf_bold())
                    .text(e.getModuleName().getString())
                    .thickness(.1f)
                    .color(new Color(200, 200, 200))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX() - 2 + (7.5f * e.getAnimation().getOutput().floatValue()), getY() + 19f + offset.get());

            QuickApi.text()
                    .size(7)
                    .font(QuickApi.sf_bold())
                    .text(KeyBoardUtil.translate(e.getKey()))
                    .thickness(.1f)
                    .color(new Color(200, 200, 200))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX() + getWidth() + 13 - (7.5f * e.getAnimation().getOutput().floatValue()) - QuickApi.inter().getWidth(KeyBoardUtil.translate(e.getKey()), 5), getY() + 19f + offset.get());

            offset.set(offset.get() + 9 * e.getAnimation().getOutput().floatValue());
        });
        ScissorUtil.pop();
    }
}
