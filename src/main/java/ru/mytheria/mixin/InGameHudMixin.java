package ru.mytheria.mixin;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.draggable.data.DraggableRepository;
import ru.mytheria.api.events.EventManager;
import ru.mytheria.api.events.impl.Render2DEvent;
import ru.mytheria.api.events.impl.RenderEvent;

@Mixin(InGameHud.class)
public class InGameHudMixin {


    @Final
    private MinecraftClient client;
    @Inject(at = @At("HEAD"), method = "render")
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (client != null && client.getWindow() != null) {
            Mytheria.getInstance().getEventProvider().post(new Render2DEvent(
                    context.getMatrices(),
                    tickCounter,
                    client.getWindow().getScaledWidth(),
                    client.getWindow().getScaledHeight()
            ));
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void renderBeforeHudInvokeMethod( DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        EventManager.call(new RenderEvent.BeforeHud(context, tickCounter));
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void renderAfterHudInvokeMethod(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        DraggableRepository draggableRepository = Mytheria.getInstance().getDraggableRepository();
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!(mc.currentScreen instanceof ChatScreen))
            draggableRepository.render(context, tickCounter, mc.mouse.getX(), mc.mouse.getY());

        EventManager.call(new RenderEvent.AfterHud(context, tickCounter));
    }
}
