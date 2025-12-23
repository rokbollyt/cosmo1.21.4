package ru.mytheria.main.module.render;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

import static net.minecraft.entity.effect.StatusEffects.NIGHT_VISION;

public class FullBright extends Module {
    public FullBright() {super(Text.of("FullBright"), null, Category.RENDER);}
    @Override
    public void activate() {
        if (mc.player == null) return;
        mc.player.addStatusEffect(new StatusEffectInstance(NIGHT_VISION, -1, 3));}
    @Override
    public void deactivate() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(NIGHT_VISION);}
}
//тест