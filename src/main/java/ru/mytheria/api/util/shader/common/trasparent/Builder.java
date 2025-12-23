package ru.mytheria.api.util.shader.common.trasparent;

import com.google.common.base.Suppliers;

import org.spongepowered.asm.mixin.Shadow;
import ru.mytheria.api.util.fonts.main.MsdfFont;
import ru.mytheria.api.util.shader.impl.blur.Blur;
import ru.mytheria.api.util.shader.impl.border.Border;
import ru.mytheria.api.util.shader.impl.glass.Glass;
import ru.mytheria.api.util.shader.impl.rect.Rectangle;
import ru.mytheria.api.util.shader.impl.text.TextSystem;
import ru.mytheria.api.util.shader.impl.texture.Texture;

import java.util.function.Supplier;

public final class Builder {

    public static final Rectangle RECTANGLE_BUILDER = new Rectangle();
    public static final Border BORDER_BUILDER = new Border();
    public static final Texture TEXTURE_BUILDER = new Texture();
    public static final TextSystem TEXT_BUILDER = new TextSystem();
    public static final Glass GLASS_BUILDER = new Glass();
    public static final Blur BLUR_BUILDER = new Blur();
    public static final Supplier<MsdfFont> INTER = Suppliers.memoize(() -> MsdfFont.builder().atlas("sf_medium").data("sf_medium").name("sf_medium").build());
    public static final Supplier<MsdfFont> SF_REGULAR = Suppliers.memoize(() -> MsdfFont.builder().atlas("sf_regular").data("sf_regular").name("sf_regular").build());
    public static final Supplier<MsdfFont> SF_SEMIBOLD = Suppliers.memoize(() -> MsdfFont.builder().atlas("sf_semibold").data("sf_semibold").name("sf_semibold").build());
    public static final Supplier<MsdfFont> SF_BOLD = Suppliers.memoize(() -> MsdfFont.builder().atlas("sf_bold").data("sf_bold").name("sf_bold").build());
    public static final Supplier<MsdfFont> ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons").data("icons").name("icons").build());
    public static final Supplier<MsdfFont> HUD_ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("hudicons").data("hudicons").name("hudicons").build());
}