package fun.cosmo.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.cosmo.Mytheria; // Добавьте импорт
import fun.cosmo.api.events.impl.EventTick; // Добавьте импорт
import fun.cosmo.api.module.settings.impl.BooleanSetting;
import fun.cosmo.main.module.render.WorldTweaks;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void mytheria$onTick(CallbackInfo ci) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.world != null) {
            Mytheria.getInstance().getEventProvider().post(new EventTick());
        }
        WorldTweaks module = WorldTweaks.getInstance();
        if (module == null || !module.isEnabled()) return;

        BooleanSetting timeSetting = module.modeSetting.get("Время");
        if (timeSetting != null && timeSetting.getEnabled()) {
            float time24 = module.timeSetting.getValue().floatValue();
            long customTime = (long) (time24 * 1000L);
            ((ClientWorld) (Object) this).setTime(customTime, customTime, false);
        }
    }
}