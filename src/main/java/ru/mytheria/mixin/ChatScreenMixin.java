package ru.mytheria.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.draggable.data.DraggableRepository;
import ru.mytheria.api.events.EventManager;
import ru.mytheria.api.events.impl.MouseEvent;
import ru.mytheria.api.events.impl.RenderEvent;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.util.shader.common.trasparent.Builder;
import ru.mytheria.main.ui.elements.window.InterfaceHandler;
import ru.mytheria.main.ui.elements.window.InterfaceWindow;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    protected ChatScreenMixin() {
        super(Text.empty());
     //   interfaceHandler.init();
    }

    private InterfaceHandler interfaceHandler;


    private static boolean visible = true;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderAfterChatInvokeMethod(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        DraggableRepository draggableRepository = Mytheria.getInstance().getDraggableRepository();

        draggableRepository.update(context, delta, mouseX, mouseY);
        draggableRepository.render(context, mc.getRenderTickCounter(), mouseX, mouseY);

        if (visible) {
            Builder.TEXT_BUILDER
                    .text("Зажмите ALT что-бы заблокировать по Y")
                    .size(8)
                    .color(0xFFFFFFFF)
                    .font(Builder.SF_SEMIBOLD.get())
                    .thickness(0.1f)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), ((float) mc.getWindow().getScaledWidth() / 2) - Builder.INTER.get().getWidth("Зажмите ALT что-бы заблокировать по Y", 8) / 2, 30);

            Builder.TEXT_BUILDER
                    .text("Нажмите CTRL + ALT что-бы активировать сетку")
                    .size(8)
                    .color(0xFFFFFFFF)
                    .font(Builder.SF_SEMIBOLD.get())
                    .thickness(0.1f)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), ((float) mc.getWindow().getScaledWidth() / 2) - Builder.INTER.get().getWidth("Нажмите CTRL + ALT что-бы переключить сетку", 8) / 2, 42);

            Builder.TEXT_BUILDER
                    .text("Нажмите CTRL + B что-бы скрыть подсказки")
                    .size(8)
                    .color(0xFFFFFFFF)
                    .font(Builder.SF_SEMIBOLD.get())
                    .thickness(0.1f)
                    .build()
                    .render(context.getMatrices().peek().getPositionMatrix(), ((float) mc.getWindow().getScaledWidth() / 2) - Builder.INTER.get().getWidth("Нажмите CTRL + ALT что-бы переключить сетку", 8) / 2, 54);
        }



        if (interfaceHandler != null) {
            int mx = (int) mc.mouse.getX();
            int my = (int) mc.mouse.getY();
            float dt = mc.getRenderTickCounter().getTickDelta(true);

         //   interfaceHandler.render(context, mx, my, dt);
        }

        EventManager.call(new RenderEvent.AfterChat(context, mouseX, mouseY, delta));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        DraggableRepository draggableRepository = Mytheria.getInstance().getDraggableRepository();

        draggableRepository.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        DraggableRepository draggableRepository = Mytheria.getInstance().getDraggableRepository();

        draggableRepository.mouseReleased(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        DraggableRepository draggableRepository = Mytheria.getInstance().getDraggableRepository();

        if (draggableRepository.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(false);
        }

        if (keyCode == GLFW.GLFW_KEY_B && (Screen.hasControlDown() || (modifiers & GLFW.GLFW_MOD_CONTROL) != 0)) {
            visible = !visible;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
     /*   if (interfaceHandler != null && interfaceHandler.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }*/
    }


}
