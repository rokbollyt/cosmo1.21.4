package ru.mytheria.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("getEffectiveGravity")
    double callGetEffectiveGravity();
}
