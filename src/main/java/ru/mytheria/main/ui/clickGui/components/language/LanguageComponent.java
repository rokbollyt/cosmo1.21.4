package ru.mytheria.main.ui.clickGui.components.language;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.localization.Language;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.api.util.render.RenderEngine;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.main.ui.clickGui.ComponentBuilder;

import java.awt.*;
import java.io.IOException;
public class LanguageComponent extends Component {
    private static final Identifier RU_TEX = Identifier.of("mre", "images/ru.png");
    private static final Identifier EN_TEX = Identifier.of("mre", "images/en.png");

    @Override
    public LanguageComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        Language lang = Mytheria.getInstance().getLanguage();
        Identifier texId = (lang == Language.RUS) ? RU_TEX : EN_TEX;

        AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(texId);
        if (texture != null) {
            RenderEngine.drawTexture(context.getMatrices(), 13, getY() + 503, 35, 25, 3f, texture, Color.white);
        }
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, 13, getY() + 503, 35, 25) && button == 0) {
            Mytheria.getInstance().toggleLanguage();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
