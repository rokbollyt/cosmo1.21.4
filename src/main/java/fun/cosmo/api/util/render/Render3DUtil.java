package fun.cosmo.api.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import fun.cosmo.api.util.color.ColorUtil; // Ваш ColorUtil

import java.util.*;

public final class Render3DUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Tessellator TESSELLATOR = Tessellator.getInstance();
    private static final Identifier PARTICLE_TEXTURE = Identifier.of("mre", "images/bloom.png"); // ПУТЬ ПРАВИЛЬНЫЙ!

    private static final List<Texture> TEXTURES = new ArrayList<>();
    private static final List<Texture> TEXTURES_DEPTH = new ArrayList<>();

    // --- Система частиц ---
    public static final List<Particle> WORLD_PARTICLES = new ArrayList<>();
    private static final Random PARTICLE_RANDOM = new Random();

    private Render3DUtil() {}

    /* ==========================
       PUBLIC API
       ========================== */

    public static void drawTexture(MatrixStack.Entry entry,
                                   Identifier texture,
                                   float x, float y,
                                   float w, float h,
                                   Vector4i color,
                                   boolean depth) {

        Texture t = new Texture(entry, texture, x, y, w, h, color);
        if (depth) TEXTURES_DEPTH.add(t);
        else TEXTURES.add(t);
    }

    /* ==========================
       CALL FROM WORLD RENDER
       ========================== */

    public static void render() {
        renderBatch(TEXTURES, false);
        renderBatch(TEXTURES_DEPTH, true);
    }

    /* ==========================
       INTERNAL (ИСПРАВЛЕНО ТОЛЬКО BLEND)
       ========================== */

    private static void renderBatch(List<Texture> batch, boolean depth) {
        if (batch.isEmpty()) return;

        RenderSystem.enableBlend();

        // ИЗМЕНЕНИЕ: Используем аддитивный бленд для 'bloom' эффекта (свечения)
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE // ИСПРАВЛЕНО: Меняем на ONE для свечения
        );

        if (depth) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.disableDepthTest();
        }

        // Оставляем ваш ShaderProgramKeys, как вы просили:
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        Set<Identifier> ids = new LinkedHashSet<>();
        for (Texture t : batch) ids.add(t.id);

        for (Identifier id : ids) {
            RenderSystem.setShaderTexture(0, id); // Эта строка загружает текстуру перед рендером

            BufferBuilder buffer = TESSELLATOR.begin(
                    VertexFormat.DrawMode.QUADS,
                    VertexFormats.POSITION_TEXTURE_COLOR
            );

            for (Texture t : batch) {
                if (!t.id.equals(id)) continue;
                quad(buffer, t);
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        if (depth) {
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
        }

        RenderSystem.disableBlend();
        batch.clear();
    }

    private static void quad(BufferBuilder b, Texture t) {
        Matrix4f m = t.entry.getPositionMatrix();

        // Нижний левый (0, 0)
        b.vertex(m, t.x, t.y + t.h, 0)
                .texture(0, 0)
                .color(t.color.x);

        // Нижний правый (1, 0)
        b.vertex(m, t.x + t.w, t.y + t.h, 0)
                .texture(1, 0)
                .color(t.color.y);

        // Верхний правый (1, 1)
        b.vertex(m, t.x + t.w, t.y, 0)
                .texture(1, 1)
                .color(t.color.z);

        // Верхний левый (0, 1)
        b.vertex(m, t.x, t.y, 0)
                .texture(0, 1)
                .color(t.color.w);
    }

    /* ==========================
       PARTICLE SYSTEM (ДЛЯ WORLD TWEAKS)
       ========================== */

    public static void updateAndDrawWorldParticles(MatrixStack matrices, float tickDelta, int maxParticles, float particleSpeed, float particleSize, int baseColor) {
        if (mc.player == null || mc.world == null) {
            WORLD_PARTICLES.clear();
            return;
        }

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Vec3d playerPos = mc.player.getPos();

        // Генерация новых частиц
        int particlesToAdd = maxParticles - WORLD_PARTICLES.size();
        for (int i = 0; i < Math.min(particlesToAdd, 5); i++) {
            double spawnX = playerPos.x + (PARTICLE_RANDOM.nextDouble() - 0.5) * 40.0;
            double spawnY = playerPos.y + (PARTICLE_RANDOM.nextDouble() - 0.5) * 20.0;
            double spawnZ = playerPos.z + (PARTICLE_RANDOM.nextDouble() - 0.5) * 40.0;
            Vec3d pos = new Vec3d(spawnX, spawnY, spawnZ);

            double velX = (PARTICLE_RANDOM.nextDouble() - 0.5) * 0.02 * particleSpeed;
            double velY = (PARTICLE_RANDOM.nextDouble() * 0.03 + 0.01) * particleSpeed;
            double velZ = (PARTICLE_RANDOM.nextDouble() - 0.5) * 0.02 * particleSpeed;
            Vec3d vel = new Vec3d(velX, velY, velZ);

            int maxLife = 50 + PARTICLE_RANDOM.nextInt(100);
            WORLD_PARTICLES.add(new Particle(pos, vel, baseColor, particleSize, maxLife));
        }

        // Обновление и рендер существующих частиц
        Iterator<Particle> iterator = WORLD_PARTICLES.iterator();
        while (iterator.hasNext()) {
            Particle particle = iterator.next();

            // Обновление позиции
            particle.position = particle.position.add(particle.velocity.multiply(tickDelta));

            // Проверка жизни и дальности
            particle.life--;
            if (particle.life <= 0 || playerPos.distanceTo(particle.position) > 60.0) {
                iterator.remove();
                continue;
            }

            // Альфа-канал на основе жизни (плавное затухание)
            float lifeProgress = (float) particle.life / particle.maxLife;
            // Используем вашу ColorUtil.alpha() и ColorUtil.changeAlpha()
            int particleAlpha = (int) (ColorUtil.alpha(particle.color) * Math.sin(lifeProgress * Math.PI));

            // Позиция относительно камеры
            Vec3d relativePos = particle.position.subtract(cameraPos);

            MatrixStack particleMatrices = new MatrixStack();
            particleMatrices.translate(relativePos.x, relativePos.y, relativePos.z);

            // Billboard (поворот к камере)
            particleMatrices.multiply(mc.gameRenderer.getCamera().getRotation());

            // Сдвиг для центрирования
            particleMatrices.translate(
                    -particle.size / 2.0F,
                    -particle.size / 2.0F,
                    0.0F
            );

            MatrixStack.Entry entry = particleMatrices.peek();

            // Применяем альфа-канал
            int finalColor = ColorUtil.changeAlpha(particle.color, particleAlpha);
            Vector4i colorVec = new Vector4i(finalColor, finalColor, finalColor, finalColor);

            // Рендер частицы (depth=false для отрисовки поверх всего)
            Render3DUtil.drawTexture(
                    entry,
                    PARTICLE_TEXTURE,
                    0, 0,
                    particle.size, particle.size,
                    colorVec,
                    false
            );
        }

        // ВАЖНО: Вызов общего рендера для отрисовки всех добавленных текстур
        render();
    }


    /* ==========================
       DATA (ДЛЯ ЧАСТИЦ)
       ========================== */

    public static class Particle {
        public Vec3d position;
        public Vec3d velocity;
        public int color;
        public float size;
        public int maxLife;
        public int life;

        public Particle(Vec3d position, Vec3d velocity, int color, float size, int maxLife) {
            this.position = position;
            this.velocity = velocity;
            this.color = color;
            this.size = size;
            this.maxLife = maxLife;
            this.life = maxLife;
        }
    }

    /* ==========================
       DATA (ДЛЯ ТЕКСТУР)
       ========================== */

    private record Texture(
            MatrixStack.Entry entry,
            Identifier id,
            float x, float y,
            float w, float h,
            Vector4i color
    ) {}
}