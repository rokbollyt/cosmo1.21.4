package ru.mytheria.main.ui.elements;
import com.google.common.base.Suppliers;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.ClientProvider;
import ru.mytheria.api.client.draggable.Draggable;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.events.impl.Render2DEvent;
import ru.mytheria.api.util.fonts.main.MsdfFont;
import ru.mytheria.api.util.media.MediaPlayer;
import ru.mytheria.api.util.media.MediaUtils;
import ru.mytheria.api.util.render.RenderEngine;
import ru.mytheria.api.util.shader.common.event.IRenderer;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.api.util.shader.impl.text.TextSystem;
import ru.mytheria.main.module.render.Interface;
import ru.mytheria.main.ui.elements.event.IRender;
import ru.mytheria.mixin.accessor.BossBarHudAccessor;

import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Watermark extends Draggable implements IRender {

    private static final long DISPLAY_MS = 2000L;
    private static final Color COLOR_GREEN = new Color(0xFF46FF00, true);
    private static final Color COLOR_RED   = new Color(0xFFFF4242, true);
    private static final Color COLOR_DEF  = new Color(0xFFA442FF, true);

    private static volatile String currentLabel = ClientProvider.getClientName();
    private static volatile Color currentStatusColor = COLOR_DEF;
    private static volatile long resetAt = 0L;

    private final SimpleAnimation widthAnimation;
    private final SimpleAnimation heightAnimation;

    public Watermark() {
        super(10f, 10f, 128f, 15f, () -> module.get().getEnabled() && module.get().getVisible().get("Watermark").getEnabled());
        widthAnimation  = new SimpleAnimation(0f, 30);
        heightAnimation = new SimpleAnimation(0f, 30);
    }

    private static boolean isTextureValid(@Nullable AbstractTexture texture) {
        if (texture == null) return false;
        try {
            Field field = AbstractTexture.class.getDeclaredField("glId");
            field.setAccessible(true);
            int glId = field.getInt(texture);
            return glId > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }


    public static void notifyModuleToggle(String moduleName, boolean enabled) {
        currentLabel = moduleName + (enabled ? " включен!" : " выключен!");
        currentStatusColor = enabled ? COLOR_GREEN : COLOR_RED;
        resetAt = System.currentTimeMillis() + DISPLAY_MS;
    }

    private static void resetIfExpired() {
        if (resetAt != 0L && System.currentTimeMillis() >= resetAt) {
            currentLabel = "Cosmo ";
            currentStatusColor = COLOR_DEF;
            resetAt = 0L;
        }
    }

    private static final Supplier<MsdfFont> BIKO_FONT = Suppliers.memoize(
            () -> MsdfFont.builder().atlas("biko").data("biko").build()
    );
    private static final Supplier<MsdfFont> UBUNTU_FONT = Suppliers.memoize(
            () -> MsdfFont.builder().atlas("aaa").data("aaa").build()
    );
    private static final Supplier<MsdfFont> ICF = Suppliers.memoize(
            () -> MsdfFont.builder().atlas("iconf").data("iconf").build()
    );
    private static final Supplier<MsdfFont> SFREG = Suppliers.memoize(
            () -> MsdfFont.builder().atlas("sf_medium").data("sf_medium").build()
    );
    private static final Supplier<MsdfFont> SF = Suppliers.memoize(
            () -> MsdfFont.builder().atlas("sf_bold").data("sf_bold").build()
    );

    private static final float PADDING_X   = 6f;
    private static final float PADDING_Y   = 4f;
    private static final float TIME_GAP    = 4f;
    private static final float FONT_SIZE   = 9f;
    private static final float FONT_THICK  = 0.05f;
    private static final float STATUS_SIZE = 10f;
    private static final float BASE_HEIGHT = 20f;
    private float[] waveHeights;
    private float[] waveTargets;
    private long lastWaveUpdate = 0L;
    static Supplier<Interface> module = Suppliers.memoize(() -> (Interface) Mytheria.getInstance().getModuleManager().find(Interface.class));

    @Override
    public void render(DrawContext context, double mouseX, double mouseY, RenderTickCounter tickCounter) {
        renderWatermark(context.getMatrices(), context.getScaledWindowWidth());
    }

    @EventHandler
    public void onRender(Render2DEvent e) {
        renderWatermark(e.getMatrixStack(), e.getScreenWidth());
    }


    private void renderWatermark(MatrixStack ms, int screenWidth) {
        resetIfExpired();
        String mode = module.get().getMode().getValue();
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        MsdfFont fontTime  = SFREG.get();
        MsdfFont fontLabel = SFREG.get();
        MsdfFont icon = ICF.get();

        float timeW  = fontTime.getWidth(timeStr, FONT_SIZE);
        float labelW = fontLabel.getWidth(currentLabel, FONT_SIZE);
        float timeToBoxGap = 6f;

        ClientBossBar bar = getPrimaryBossBar();
        boolean showBoss = bar != null;

        MediaUtils.MediaInfo mediaInfo = MediaUtils.getCurrentMedia();
        boolean mediaInf = mediaInfo != null && mediaInfo.getTexture() != null;

        float leftW;
        float leftH;
        if (showBoss) {
            float[] size = getBossBarSize(bar);
            leftW = size[0];
            leftH = size[1];
        } else if (mediaInf) {
        String track = mediaInfo.title;
        if (mediaInfo.artist != null && !mediaInfo.artist.isEmpty()) {
            track += " - " + mediaInfo.artist;
        }

            MsdfFont mediaFont = SFREG.get();
            float trackW = mediaFont.getWidth(track, FONT_SIZE) + 5;

            int bars = 4;
        float barWidth = 3f;
        float gap = 2f;
        float waveBlockW = bars * barWidth + (bars - 1) * gap + 4f;

        leftW = STATUS_SIZE + TIME_GAP + trackW + TIME_GAP + waveBlockW;
        leftH = Math.max(STATUS_SIZE, FONT_SIZE + 2f);
        } else {
            leftW = STATUS_SIZE + TIME_GAP + labelW;
            leftH = Math.max(STATUS_SIZE, FONT_SIZE + 2f);
        }

        float targetBoxWidth  = leftW + PADDING_X * 2f + 8f;
        float targetBoxHeight = Math.max(BASE_HEIGHT, leftH + PADDING_Y * 2f) + (showBoss ? 6f : 0f);

        if (Math.abs(widthAnimation.getOutput() - targetBoxWidth) > 0.5f) {
            widthAnimation.animateTo(targetBoxWidth);
        }
        if (Math.abs(heightAnimation.getOutput() - targetBoxHeight) > 0.5f) {
            heightAnimation.animateTo(targetBoxHeight);
        }

        float boxWidth  = widthAnimation.getOutput();
        float boxHeight = heightAnimation.getOutput();

        int screenW = screenWidth;
        float X = screenW / 2f - (boxWidth + timeW + timeToBoxGap) / 2f + timeW + timeToBoxGap - 12;
        float Y = 7f;

        float timeX = X - timeW - timeToBoxGap;
        float timeY = Y + (boxHeight - FONT_SIZE) / 2f + 1f;

        new TextSystem()
                .font(SF.get())
                .text(timeStr)
                .color(Color.black)
                .size(FONT_SIZE)
                .thickness(FONT_THICK)
                .build()
                .render(ms.peek().getPositionMatrix(), timeX - 3, timeY - 2);

        if (mode.equals("Стекло")) {
            drawGlass(ms, X, Y, boxWidth - 3, boxHeight);
        }

        if (mode.equals("Блюр")) {
            drawBlur(ms, X, Y, boxWidth - 3, boxHeight);
        }
        float cx = X + PADDING_X;
        float cy = Y + (boxHeight - leftH) / 2f;

        if (showBoss) {
            renderBossbar(ms, bar, cx + 2f, cy + 15f, leftW, 8f);
        } else {
            if (mediaInf) {
                float textureSize = STATUS_SIZE + 4;
                float textureX = cx;
                float textureY = cy + (leftH - textureSize) / 2f;
                String track = mediaInfo.title;
                if (mediaInfo.artist != null && !mediaInfo.artist.isEmpty()) {
                    track += " - " + mediaInfo.artist;
                }
                MsdfFont mediaFont = SFREG.get();
                float trackW = mediaFont.getWidth(track, FONT_SIZE) + 5;
                float textEndX = cx + STATUS_SIZE + TIME_GAP + trackW + TIME_GAP;
                RenderEngine.drawTexture(ms, textureX - 3.5f, textureY - 1f, textureSize, textureSize, 5.5f, mediaInfo.getTexture(), new Color(255, 255, 255, 255));

                int bars = 4;
                float barWidth = 3f;
                float gap = 2f;
                float baseX = cx + STATUS_SIZE + TIME_GAP;
                float baseY = cy + leftH - 2f;



                if (waveHeights == null || waveHeights.length != bars) {
                    waveHeights = new float[bars];
                    waveTargets = new float[bars];
                    for (int i = 0; i < bars; i++) {
                        waveHeights[i] = 5f;
                        waveTargets[i] = 5f;
                    }
                }

                if (System.currentTimeMillis() - lastWaveUpdate > 150) {
                    lastWaveUpdate = System.currentTimeMillis();
                    for (int i = 0; i < bars; i++) {
                        waveTargets[i] = 3f + (float) (Math.random() * 10f);
                    }
                }

                for (int i = 0; i < bars; i++) {
                    waveHeights[i] += (waveTargets[i] - waveHeights[i]) * 1f;

                    RenderEngine.drawRectangle(
                            ms,
                            textEndX + i * (barWidth + gap) + 6,
                            baseY - waveHeights[i] + 2,
                            barWidth - 1,
                            waveHeights[i] - 1,
                            new ColorState(new Color(255, 255, 255, 255)),
                            new RadiusState(0.9f),
                            1.0f,
                            100f
                    );
                }

                if (track != null && !track.isEmpty()) {
                    new TextSystem()
                            .font(SF.get())
                            .text(track)
                            .color(Color.WHITE)
                            .size(FONT_SIZE)
                            .thickness(FONT_THICK)
                            .build()
                            .render(ms.peek().getPositionMatrix(),
                                    cx + STATUS_SIZE + TIME_GAP - 1,
                                    cy - 1f + (leftH - FONT_SIZE) / 2f - 1.2
                            );
                }


            } else {
                RenderEngine.drawRectangle(ms,
                        cx - 1,
                        cy + (leftH - STATUS_SIZE) / 2f - 1,
                        STATUS_SIZE,
                        STATUS_SIZE,
                        new ColorState(currentStatusColor),
                        new RadiusState(4),
                        1.0f,
                        75f);

                new TextSystem()
                        .font(SFREG.get())
                        .text(currentLabel)
                        .color(Color.WHITE)
                        .size(FONT_SIZE)
                        .thickness(FONT_THICK)
                        .build()
                        .render(
                                ms.peek().getPositionMatrix(),
                                cx + STATUS_SIZE + TIME_GAP - 1,
                                cy - 1f + (leftH - FONT_SIZE) / 2f - 1.2
                        );
            }
        }

        new TextSystem()
                .font(icon)
                .text("D")
                .color(Color.black)
                .size(FONT_SIZE + 2)
                .thickness(FONT_THICK - 0.010f)
                .build()
                .render(
                        ms.peek().getPositionMatrix(),
                        X + boxWidth + 2f, timeY - 4
                );
    }




    private void renderBossbar(MatrixStack ms, ClientBossBar bar, float x, float y, float w, float h) {
        String rawText = bar.getName().getString();
        MsdfFont font = UBUNTU_FONT.get();

        Pattern pattern = Pattern.compile("(\\d+)(?!.*\\d)");
        Matcher matcher = pattern.matcher(rawText);
        String timeStr = "??s";
        if (matcher.find()) {
            timeStr = matcher.group(1) + "s";
        }

        String labelText = rawText
                .replaceAll(",?\\s*до конца.*$", "")
                .replaceAll("Телепортация.*", "Телепортация")
                .trim();

        float spacing = 6f;

        float timeBoxW = font.getWidth(timeStr, FONT_SIZE) + 10f;
        float labelW   = font.getWidth(labelText, FONT_SIZE);
        float totalW   = timeBoxW + spacing + labelW;

        float centerX = x + (w - totalW) / 2f;
        float timeBoxX = centerX;
        float labelX   = timeBoxX + timeBoxW + spacing;
        float textY    = y - FONT_SIZE - 4f;

        RenderEngine.drawRectangle(ms,
                timeBoxX,
                textY - 2f,
                timeBoxW,
                FONT_SIZE + 4f,
                new ColorState(COLOR_RED),
                new RadiusState(4),
                1.0f,
                75f);

        new TextSystem()
                .font(SFREG.get())
                .text(timeStr)
                .color(Color.WHITE)
                .size(FONT_SIZE)
                .thickness(FONT_THICK)
                .build()
                .render(ms.peek().getPositionMatrix(), timeBoxX + 5f, textY - 1);

        new TextSystem()
                .font(SFREG.get())
                .text(labelText)
                .color(Color.WHITE)
                .size(FONT_SIZE)
                .thickness(FONT_THICK)
                .build()
                .render(ms.peek().getPositionMatrix(), labelX, textY - 1);
    }



    private @Nullable ClientBossBar getPrimaryBossBar() {
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) hud.getBossBarHud()).getBossBars();
        if (bars == null || bars.isEmpty()) return null;
        return bars.values().iterator().next();
    }

    private void drawGlass(MatrixStack ms, float x, float y, float w, float h) {

        QuickApi.blur()
                .size(new SizeState(w, h - 2))
                .radius(new RadiusState(8))
                .blurRadius(10)
                .build()
                .render(ms.peek().getPositionMatrix(), x, y);


        RenderEngine.drawLiquid(ms,
                x, y, w - 2, h - 2,
                new ColorState(new Color(200, 200, 200)),
                new RadiusState(8),
                1f,   // smoothness
                9.0f,  // glassDirection
                25.0f,  // glassQuality
                1.2f    // glassSize
        );
    }

    private void drawBlur(MatrixStack ms, float x, float y, float w, float h) {
        RenderEngine.drawBlurRectangle(                ms,
                x, y, w - 2, h - 2,
                new ColorState(new Color(0xFF464646, true)),
                new RadiusState(7.5),
                1f,
                55,
                0.1f);
    }



    private static class SimpleAnimation {
        private float currentValue;
        private float targetValue;
        private final long durationMs;
        private long startTime;
        private boolean running;

        public SimpleAnimation(float startValue, long durationMs) {
            this.currentValue = startValue;
            this.targetValue = startValue;
            this.durationMs = durationMs;
            this.running = false;
        }

        public void animateTo(float newTarget) {
            this.targetValue = newTarget;
            this.startTime = System.currentTimeMillis();
            this.running = true;
        }

        public float getOutput() {
            if (!running) return currentValue;

            long elapsed = System.currentTimeMillis() - startTime;
            float t = Math.min(1f, (float) elapsed / durationMs);

            currentValue = currentValue + t * (targetValue - currentValue);

            if (t >= 1f) {
                running = false;
                currentValue = targetValue;
            }

            return currentValue;
        }
    }

    private float[] getBossBarSize(ClientBossBar bar) {
        String rawText = bar.getName().getString();
        MsdfFont font = UBUNTU_FONT.get();

        String timeStr = "??s";
        Matcher matcher = Pattern.compile("(\\d+)(?!.*\\d)").matcher(rawText);
        if (matcher.find()) timeStr = matcher.group(1) + "s";

        String labelText = rawText
                .replaceAll(",?\\s*до конца.*$", "")
                .replaceAll("Телепортация.*", "Телепортация")
                .trim();

        float timeW = font.getWidth(timeStr, FONT_SIZE) + 10f;
        float labelW = font.getWidth(labelText, FONT_SIZE);
        float totalW = timeW + 6f + labelW;
        float totalH = FONT_SIZE + 4f;

        return new float[] { totalW, totalH };
    }

}
