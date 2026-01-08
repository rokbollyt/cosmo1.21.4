package fun.cosmo.main.module.render;

// Импорт TargetEntitySelector нужен, чтобы получить цель
import fun.cosmo.main.module.combat.aura.util.TargetEntitySelector;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import fun.cosmo.api.events.impl.EventRender3D;
import fun.cosmo.api.module.Category;
import fun.cosmo.api.module.Module;
import fun.cosmo.api.module.settings.impl.ModeSetting;
import fun.cosmo.api.util.render.Render3DUtil;
import fun.cosmo.api.util.color.ColorUtil;

public class TargetESP extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // --- КОНСТАНТЫ ДЛЯ ТЕКСТУР ---
    private static final Identifier BLOOM_TEXTURE =
            Identifier.of("mre", "images/bloom.png");
    private static final Identifier SNOW_TEXTURE =
            Identifier.of("mre", "images/snow.png");
    // ----------------------------

    private float anim = 0f;
    // ✅ КОНСТАНТА УВЕЛИЧЕНИЯ РАЗМЕРА: 1.0f - оригинальный, 2.0f - в два раза жирнее
    private static final float SIZE_MULTIPLIER = 2.0f;

    // --- НАСТРОЙКА: ВЫБОР ТЕКСТУРЫ ---
    public ModeSetting particleTextureSetting = new ModeSetting(
            Text.of("Эффект"),
            Text.of("Выбирает текстуру следа"),
            () -> true
    )
            .set("Обычный", "Новогодний")
            .setDefault("Обычный");
    // ------------------------------------

    public TargetESP() {
        super(Text.of("TargetESP"), Category.RENDER);
        this.getSettingLayers().add(particleTextureSetting);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (!getEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        // ✅ ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД getTarget(), который берет цель из селектора
        LivingEntity target = getTarget();
        if (target == null) return;

        anim += e.getPartialTicks() * 0.05f;

        drawGhostTrail(e, target);
        Render3DUtil.render();
    }

    private Identifier getCurrentTexture() {
        if (particleTextureSetting.getValue().equals("Новогодний")) {
            return SNOW_TEXTURE;
        }
        return BLOOM_TEXTURE;
    }

    private void drawGhostTrail(EventRender3D e, LivingEntity target) {
        Camera cam = e.getCamera();
        MatrixStack matrices = e.getMatrixStack();

        int baseRgbColor = ColorUtil.getClientColor() & 0x00FFFFFF;

        // интерполяция позиции
        Vec3d targetPos = new Vec3d(
                target.prevX + (target.getX() - target.prevX) * e.getPartialTicks(),
                target.prevY + (target.getY() - target.prevY) * e.getPartialTicks(),
                target.prevZ + (target.getZ() - target.prevZ) * e.getPartialTicks()
        );

        targetPos = targetPos.add(0, target.getHeight() * 0.55, 0);

        Vec3d camPos = cam.getPos();
        Vec3d base = targetPos.subtract(camPos);

        int ghostsPerRing = 12;
        int rings = 3;

        float radius = target.getWidth() * 1.1f;
        float delay = 0.22f;

        Identifier texture = getCurrentTexture();

        for (int ring = 0; ring < rings; ring++) {

            float ringPhase = (float) (Math.PI * 2 / rings) * ring;
            float ringYOffset = (ring - 1) * 0.25f;

            for (int i = 0; i < ghostsPerRing; i++) {

                float time = anim - i * delay + ringPhase;

                double x = Math.cos(time) * radius;
                double z = Math.sin(time) * radius;

                double y = Math.sin(time * 1.4f) * 0.35 + ringYOffset;

                float alpha = 1f - (i / (float) ghostsPerRing);

                float size = (0.38f * alpha) * SIZE_MULTIPLIER;

                matrices.push();
                matrices.translate(
                        base.x + x,
                        base.y + y,
                        base.z + z
                );

                // billboard
                matrices.multiply(
                        net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
                matrices.multiply(
                        net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));

                int a = (int) (255 * alpha);

                int color = (a << 24) | baseRgbColor;

                Render3DUtil.drawTexture(
                        matrices.peek(),
                        texture,
                        -size / 2f,
                        -size / 2f,
                        size,
                        size,
                        new org.joml.Vector4i(color),
                        false
                );

                matrices.pop();
            }
        }
    }

    // --- НОВЫЙ МЕТОД: ПОЛУЧЕНИЕ ТЕКУЩЕЙ ЦЕЛИ ---
    private LivingEntity getTarget() {
        // 1. Получите экземпляр TargetEntitySelector, который используется вашим боевым модулем
        //    (Например, если KillAura сохраняет его статически или через сервис)
        TargetEntitySelector selector = getAuraSelector();

        // 2. Если селектор недоступен или не имеет цели, возвращаем null
        if (selector == null) return null;

        // 3. Возвращаем уже выбранную цель
        LivingEntity target = selector.getCurrentTarget();

        // Дополнительная проверка на null и на игрока
        if (target == null || target == mc.player) return null;

        return target;
    }

    /**
     * !!! ЗАГЛУШКА !!!
     * * Этот метод должен быть заменен на реальный способ получения
     * TargetEntitySelector, который используется вашим боевым модулем (KillAura).
     * * @return Экземпляр TargetEntitySelector или null, если он не инициализирован.
     */
    private TargetEntitySelector getAuraSelector() {
        // Пример: Если у вас есть статический класс CombatManager:
        // return CombatManager.getKillAura().getTargetSelector();

        // Если вы не знаете, как получить его, вы можете временно вернуть
        // заглушку или (если ваш фреймворк поддерживает) попытаться найти модуль

        // ВАРИАНТ 1: Если KillAura открывает свой селектор
        // Module killAura = mc.getModuleManager().getModule(KillAura.class);
        // if (killAura instanceof KillAura kA) {
        //     return kA.getTargetSelector();
        // }

        // Для этого примера вернем null, предполагая, что KillAura
        // должна предоставить цель.
        return null;
    }
    // ---------------------------------------------
}