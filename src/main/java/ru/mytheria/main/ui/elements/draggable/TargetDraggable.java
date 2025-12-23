package ru.mytheria.main.ui.elements.draggable;

import com.google.common.base.Suppliers;
import lombok.Getter;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.draggable.Draggable;
import ru.mytheria.api.clientannotation.QuickApi;
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

public class TargetDraggable extends Draggable {

    float animationValue;

    static Supplier<Interface> module = Suppliers.memoize(() -> (Interface) Mytheria.getInstance().getModuleManager().find(Interface.class));

    public TargetDraggable() {
        super(10f, 25f, 95f, 33f, () ->
                module.get().getEnabled() &&
                        module.get().getVisible().get("Target").getEnabled()
        );
    }

    @NonFinal
    @Getter
    Entity target = null;

    @Override
    public void render( DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter) {

        LivingEntity target = mc.currentScreen instanceof ChatScreen ? mc.player : findNearestEntity(10);
        if (target == null) target = mc.player;
        String mode = module.get().getMode().getValue();
        animationValue = MathHelper.lerp(0.3f, animationValue, MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0f, 1f));
        MatrixStack matrices = context.getMatrices();
        if (mode.equals("Блюр")) {

            QuickApi.border()
                    .radius(new RadiusState(11))
                    .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, 40)))
                    .thickness(-.5f)
                    .size(new SizeState(getWidth() + 5, getHeight()))
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

            RenderEngine.drawBlurRectangle(context.getMatrices(),
                    getX(), getY(), getWidth() + 5, getHeight(),
                    new ColorState(new Color(0xFF464646, true)),
                    new RadiusState(11),
                    1f,
                    55,
                    0.1f);

        }
        else if (mode.equals("Стекло")) {

           QuickApi.blur()
                    .size(new SizeState(getWidth() + 5, getHeight()))
                    .radius(new RadiusState(12))
                    .blurRadius(10)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());


            RenderEngine.drawLiquid(context.getMatrices(),
                    getX(), getY(), getWidth() + 5, getHeight(),
                    new ColorState(new Color(200, 200, 200)),
                    new RadiusState(12),
                    1f,   // smoothness
                    9.0f,  // glassDirection
                    25.0f,  // glassQuality
                    1.2f    // glassSize
            );
        }

        QuickApi.text()
                .font(QuickApi.inter())
                .text(target.getName().getString())
                .color(new Color(255, 255, 255))
                .size(8)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 30.5f, getY() + 6);

        if (target instanceof AbstractClientPlayerEntity player) {
            RenderEngine.drawTexture(
                    context.getMatrices(),
                    getX() + 6,
                    getY() + 6,
                    21,
                    21,
                    6f,
                    0.125f,
                    0.125f,
                    0.125f,
                    0.125f,
                    player.getSkinTextures().texture(),
                    Color.WHITE
            );
        } else {

                    RenderEngine.drawRectangle(context.getMatrices(),
                            getX() + 6, getY() + 6, 21, 21,
                            new ColorState(new Color(0, 0, 0, 119)),
                            new RadiusState(7f),
                            1f
                    );

            QuickApi.text()
                    .font(QuickApi.inter())
                    .text("?")
                    .color(new Color(200, 200, 200))
                    .size(15)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), getX() + 12, getY() + 7.5);

        }


        float maxWidth = 60f;
        float barWidth = maxWidth * animationValue;


        RenderEngine.drawRectangle(context.getMatrices(),
                getX() + 30.3f, getY() + 19, barWidth, 6f,
                new ColorState(new Color(83, 0, 255)),
                new RadiusState(2),
                1f
        );

        RenderEngine.drawRectangle(context.getMatrices(),
                getX() + 30.3f, getY() + 19, maxWidth, 6.3f,
                new ColorState(new Color(255, 255, 255, 34)),
                new RadiusState(2),
                1f
        );

    }

    private LivingEntity findNearestEntity(double radius) {
        LivingEntity nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living && entity != mc.player && !entity.isSpectator()) {
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist < radius * radius && dist < minDist) {
                    nearest = living;
                    minDist = dist;
                }
            }
        }

        return nearest;
    }
}
