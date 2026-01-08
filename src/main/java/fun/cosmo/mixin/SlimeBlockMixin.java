package fun.cosmo.mixin;

import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.cosmo.main.module.movement.SlimeBooster;
import net.minecraft.client.MinecraftClient; // Импорт для проверки игрока

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {

    @Inject(
            method = "onEntityLand(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mytheria$applyClientSlimeBoost(BlockView world, Entity entity, CallbackInfo ci) {
        SlimeBooster module = SlimeBooster.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (module == null || !module.isEnabled()) {
            return;
        }

        // 🔥🔥 КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: СРАБАТЫВАЕТ ТОЛЬКО НА КЛИЕНТСКОМ ИГРОКЕ 🔥🔥
        // Мы применяем логику только к нашему игроку, чтобы избежать киков,
        // но при этом игрок видит эффект.
        if (entity != mc.player || entity.bypassesLandingEffects()) {
            return;
        }

        Vec3d velocity = entity.getVelocity();

        // Ванильная логика отскока (bounce) срабатывает, только если velocity.y < 0.0F
        if (velocity.y < (double)0.0F) {

            float boostMultiplier = module.getBoostHeight();

            // Расчет новой Y-скорости
            double newYVelocity = Math.abs(velocity.y) * boostMultiplier;

            // Установка скорости на стороне клиента (для предсказания)
            entity.setVelocity(velocity.x, newYVelocity, velocity.z);

            // Вызываем onLanding() для сброса fallDistance
            entity.onLanding();

            // Отменяем ванильный вызов bounce()
            ci.cancel();
        }
    }
}