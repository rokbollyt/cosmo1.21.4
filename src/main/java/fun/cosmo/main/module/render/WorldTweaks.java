package fun.cosmo.main.module.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera; // Добавлен импорт
import net.minecraft.client.util.math.MatrixStack; // Добавлен импорт
import net.minecraft.text.Text;
import net.minecraft.util.Identifier; // Добавлен импорт
import net.minecraft.util.math.RotationAxis; // Добавлен импорт
import net.minecraft.util.math.Vec3d; // Добавлен импорт
import org.joml.Vector4i; // Добавлен импорт
import fun.cosmo.Mytheria;
import fun.cosmo.api.events.EventManager;
import fun.cosmo.api.events.impl.EventRender3D;
import fun.cosmo.api.events.impl.FogEvent;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.impl.BooleanSetting;
import fun.cosmo.api.module.settings.impl.ModeListSetting;
import fun.cosmo.api.module.settings.impl.ModeSetting;
import fun.cosmo.api.module.settings.impl.SliderSetting;
import fun.cosmo.api.util.color.ColorUtil;
import fun.cosmo.api.util.render.Render3DUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorldTweaks extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // --- ПЕРЕНЕСЕННЫЕ ПОЛЯ ДЛЯ ЧАСТИЦ (Добавлено для всех трех типов) ---
    private static final Identifier BLOOM_TEXTURE = Identifier.of("mre", "images/bloom.png");
    private static final Identifier SNOW_TEXTURE = Identifier.of("mre", "images/snow.png");
    private static final Identifier STAR_TEXTURE = Identifier.of("mre", "images/star.png");

    private final List<Particle> WORLD_PARTICLES = new ArrayList<>();
    private final Random PARTICLE_RANDOM = new Random();

    // Вспомогательный класс Particle, теперь внутри WorldTweaks (или в отдельном файле)
    private static class Particle {
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
    // ------------------------------------

    public static WorldTweaks getInstance() {
        return (WorldTweaks) Mytheria.getInstance().getModuleManager().find(WorldTweaks.class);
    }

    // --- НАСТРОЙКИ (Ваш код без изменений) ---

    public ModeListSetting modeSetting = new ModeListSetting(Text.of("World tweaks"), Text.of("Позволяет настроить элементы мира"), () -> true)
            .set("Время", "Туман", "Частицы");

    public SliderSetting timeSetting = new SliderSetting(Text.of("Время суток"), Text.of("Устанавливает значение времени суток (0=полночь, 12=полдень)"), () -> modeSetting.get("Время").getEnabled())
            .set(0.0F, 24.0F, 1.0F)
            .set(12.0F);

    public SliderSetting distanceSetting = new SliderSetting(Text.of("Дистанция тумана"), Text.of("Устанавливает дальность прорисовки тумана"), () -> modeSetting.get("Туман").getEnabled())
            .set(20.0F, 200.0F, 1.0F)
            .set(100.0F);

    public ModeSetting particleTextureSetting = new ModeSetting(Text.of("Тип частиц"), Text.of("Выбирает текстуру для мировых частиц"), () -> modeSetting.get("Частицы").getEnabled())
            .set("Snow", "Bloom", "Star")
            .set("Bloom");

    public SliderSetting particleCountSetting = new SliderSetting(Text.of("Количество частиц"), Text.of("Устанавливает максимальное количество частиц в мире"), () -> modeSetting.get("Частицы").getEnabled())
            .set(10.0F, 500.0F, 1.0F)
            .set(100.0F);

    public SliderSetting particleSpeedSetting = new SliderSetting(Text.of("Скорость частиц"), Text.of("Устанавливает скорость движения частиц"), () -> modeSetting.get("Частицы").getEnabled())
            .set(0.1F, 5.0F, 0.01F)
            .set(1.0F);

    public SliderSetting particleSizeSetting = new SliderSetting(Text.of("Размер частиц"), Text.of("Устанавливает визуальный размер частиц"), () -> modeSetting.get("Частицы").getEnabled())
            .set(0.05F, 0.5F, 0.01F)
            .set(0.15F);

    public WorldTweaks() {
        super(Text.of("World tweaks"), Text.of("Настройки рендера мира"), Category.RENDER);
        EventManager.register(this.getClass());

        this.getSettingLayers().add(modeSetting);
        modeSetting.getValues().forEach(this.getSettingLayers()::add);

        this.getSettingLayers().add(timeSetting);
        this.getSettingLayers().add(distanceSetting);

        this.getSettingLayers().add(particleTextureSetting);
        this.getSettingLayers().add(particleCountSetting);
        this.getSettingLayers().add(particleSpeedSetting);
        this.getSettingLayers().add(particleSizeSetting);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        WORLD_PARTICLES.clear(); // Используем локальный список
    }

    // --- ВСПОМОГАТЕЛЬНЫЙ МЕТОД ДЛЯ ВЫБОРА ТЕКСТУРЫ (ИСПРАВЛЕНО: getValue() вместо getMode()) ---
    private Identifier getCurrentTexture() {
        String mode = particleTextureSetting.getValue(); // ИСПРАВЛЕНО: используем getValue()
        switch (mode) {
            case "Snow":
                return SNOW_TEXTURE;
            case "Star":
                return STAR_TEXTURE;
            case "Bloom":
            default:
                return BLOOM_TEXTURE;
        }
    }
    // --------------------------------------------------

    // --- ОБРАБОТЧИКИ СОБЫТИЙ ---

    // 2. Туман (FogEvent) - Без изменений
    @EventHandler
    public void onFog(FogEvent e) {
        if (!isEnabled()) return;

        BooleanSetting fogSetting = modeSetting.get("Туман");
        if (fogSetting != null && fogSetting.getEnabled()) {
            e.setDistance(distanceSetting.getValue().floatValue());
            e.setColor(ColorUtil.getClientColor());
            e.cancel();
        }
    }

    // 3. Частицы (EventRender3D) - ИСПРАВЛЕНА ЛОГИКА РЕНДЕРА
    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (!isEnabled()) return;

        BooleanSetting particleSetting = modeSetting.get("Частицы");

        if (mc.player == null || mc.world == null) {
            WORLD_PARTICLES.clear();
            return;
        }

        if (particleSetting != null && particleSetting.getEnabled()) {

            // 🔥 РЕНДЕР ЧАСТИЦ ПЕРЕМЕЩЕН СЮДА И ИСПРАВЛЕН 🔥

            MatrixStack matrices = e.getMatrixStack();
            float partialTicks = e.getPartialTicks();
            int maxParticles = (int) particleCountSetting.getValue().floatValue();
            float particleSpeed = particleSpeedSetting.getValue().floatValue();
            float particleSize = particleSizeSetting.getValue().floatValue();
            int baseColor = ColorUtil.getClientColor();

            Camera cam = e.getCamera();
            Vec3d cameraPos = cam.getPos();
            Vec3d playerPos = mc.player.getPos();

            // Генерация новых частиц
            int particlesToAdd = maxParticles - WORLD_PARTICLES.size();
            // ИСПРАВЛЕНИЕ: Правильный синтаксис цикла for
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
                particle.position = particle.position.add(particle.velocity.multiply(partialTicks));

                // Проверка жизни и дальности
                particle.life--;
                if (particle.life <= 0 || playerPos.distanceTo(particle.position) > 60.0) {
                    iterator.remove();
                    continue;
                }

                // Альфа-канал на основе жизни (плавное затухание)
                float lifeProgress = (float) particle.life / particle.maxLife;
                int particleAlpha = (int) (ColorUtil.alpha(particle.color) * Math.sin(lifeProgress * Math.PI));

                // Позиция относительно камеры
                Vec3d relativePos = particle.position.subtract(cameraPos);

                matrices.push(); // Используем матричный стек из EventRender3D
                matrices.translate(relativePos.x, relativePos.y, relativePos.z); // 1. Перенос в точку мира

                // --- ЛОГИКА BILLBOARD (как в TargetESP) ---
                // 2. Поворот Y (Yaw)
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
                // 3. Поворот X (Pitch)
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));
                // --- КОНЕЦ ЛОГИКИ BILLBOARD ---

                MatrixStack.Entry entry = matrices.peek();

                // Применяем альфа-канал
                int finalColor = ColorUtil.changeAlpha(particle.color, particleAlpha);
                Vector4i colorVec = new Vector4i(finalColor, finalColor, finalColor, finalColor);

                // Рендер частицы: depth=true и центрирование через координаты
                Render3DUtil.drawTexture(
                        entry,
                        getCurrentTexture(), // ИСПОЛЬЗУЕМ ВЫБРАННУЮ ТЕКСТУРУ
                        -particle.size / 2.0F, // <-- Центрирование
                        -particle.size / 2.0F,
                        particle.size,
                        particle.size,
                        colorVec,
                        true // <-- ВАЖНО: TRUE для глубины (должно скрываться за блоками)
                );

                matrices.pop();
            }

            Render3DUtil.render(); // Вызываем общий рендер
        }
    }
}