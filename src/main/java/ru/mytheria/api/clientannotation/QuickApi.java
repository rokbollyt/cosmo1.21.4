package ru.mytheria.api.clientannotation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.mytheria.api.util.fonts.main.MsdfFont;
import ru.mytheria.api.util.shader.common.trasparent.Builder;
import ru.mytheria.api.util.shader.impl.blur.Blur;
import ru.mytheria.api.util.shader.impl.border.Border;
import ru.mytheria.api.util.shader.impl.glass.Glass;
import ru.mytheria.api.util.shader.impl.rect.Rectangle;
import ru.mytheria.api.util.shader.impl.text.TextSystem;
import ru.mytheria.api.util.shader.impl.texture.Texture;

public interface QuickApi {

    MinecraftClient mc = MinecraftClient.getInstance();

    default void print(Object o) {
        if (mc == null) return;

        mc.inGameHud.getChatHud().addMessage(Text.of(o.toString()));
    }

    static Rectangle rectangle() {
        return Builder.RECTANGLE_BUILDER;
    }

    static Border border() {
        return Builder.BORDER_BUILDER;
    }

    static Texture texture() {
        return Builder.TEXTURE_BUILDER;
    }

    static TextSystem text() { return Builder.TEXT_BUILDER; }

    static Glass glass() { return Builder.GLASS_BUILDER;}

    static Blur blur() {
        return Builder.BLUR_BUILDER;
    }

    static MsdfFont inter() { return Builder.INTER.get(); }

    static MsdfFont sf_bold() { return Builder.SF_BOLD.get(); }

    static MsdfFont sf_semi() { return Builder.SF_SEMIBOLD.get(); }

    static MsdfFont sf_reg() { return Builder.SF_REGULAR.get(); }


    static MsdfFont icons() { return Builder.ICONS.get(); }

    static MsdfFont hudIcons() { return Builder.HUD_ICONS.get(); }

}
