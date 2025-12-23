package ru.mytheria.main.ui.clickGui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.api.events.impl.KeyEvent;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;
import ru.mytheria.api.util.window.WindowRepository;
import ru.mytheria.main.module.misc.Unhook;
import ru.mytheria.main.ui.clickGui.components.language.LanguageComponent;
import ru.mytheria.main.ui.clickGui.components.panel.PanelsLayer;
import ru.mytheria.main.ui.clickGui.components.search.SearchComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static ru.mytheria.api.util.math.Math.scale;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClickGuiScreen extends Screen implements QuickImport {

    List<Component> componentsList = new ArrayList<>();
    WindowRepository windowRepository = new WindowRepository();

    PanelsLayer panelsLayer = new PanelsLayer();
    SearchComponent searchComponent = new SearchComponent();
    LanguageComponent languageComponent = new LanguageComponent();

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    Animation animation = new DecelerateAnimation()
            .setMs(150)
            .setValue(1f);

    final float width = 645f;
    final float height = 550f / 2;

    Supplier<Float> x = () -> (mc.getWindow().getScaledWidth() - width) / 2;
    Supplier<Float> y = () -> (mc.getWindow().getScaledHeight() - height) / 2;

    public ClickGuiScreen() {
        super(Text.of("pasxalka.click_gui"));

        componentsList.addAll(List.of(
                panelsLayer,
                searchComponent,
                languageComponent
        ));

        Mytheria.getInstance().getEventProvider().subscribe(this);
    }

    @Override
    protected void init() {
        componentsList.forEach(Component::init);
        animation.setDirection(Direction.FORWARDS);
        animation.reset();
        super.init();
    }

    @Override
    public void close() {
        animation.setDirection(Direction.BACKWARDS);
        animation.reset();
        windowRepository.close();
    }

    @Override
    public boolean shouldPause() {
        return false; // ДВИЖЕНИЕ РАБОТАЕТ
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        // ⛔ ЕСЛИ UNHOOK — GUI ДАЖЕ НЕ РИСУЕТСЯ
        if (Unhook.ACTIVE) return;

        if (animation.isFinished(Direction.BACKWARDS)) {
            mc.setScreen(null);
            return;
        }

        panelsLayer.position(x.get(), y.get()).size(width, height);
        searchComponent.position(x.get() + width / 2 - 50, y.get() + height + 25).size(100, 20);

        scale(context.getMatrices(),
                x.get() + width / 2,
                y.get() + height / 2,
                animation.getOutput().floatValue(),
                () -> {
                    componentsList.forEach(e -> e.render(context, mouseX, mouseY, delta));
                    windowRepository.render(context, mouseX, mouseY, delta);
                });

        super.render(context, mouseX, mouseY, delta);
    }

    // ⛔ RIGHT SHIFT МЁРТВ ПРИ UNHOOK
    @EventHandler
    public void keyListener(KeyEvent keyEvent) {

        if (Unhook.ACTIVE) return;

        if (mc.currentScreen == null
                && keyEvent.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(Mytheria.getInstance().getClickGuiScreen());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Unhook.ACTIVE) return false;

        if (!windowRepository.mouseClicked(mouseX, mouseY, button))
            componentsList.forEach(e -> e.mouseClicked(mouseX, mouseY, button));

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (Unhook.ACTIVE) return false;

        if (!windowRepository.mouseReleased(mouseX, mouseY, button))
            componentsList.forEach(e -> e.mouseReleased(mouseX, mouseY, button));

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Unhook.ACTIVE) return false;

        if (!windowRepository.keyPressed(keyCode, scanCode, modifiers))
            componentsList.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (Unhook.ACTIVE) return false;

        if (!windowRepository.keyPressed(keyCode, scanCode, modifiers))
            componentsList.forEach(e -> e.keyReleased(keyCode, scanCode, modifiers));

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (Unhook.ACTIVE) return false;

        componentsList.forEach(e -> e.charTyped(chr, modifiers));
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Unhook.ACTIVE) return false;

        if (!windowRepository.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            componentsList.forEach(e -> e.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
