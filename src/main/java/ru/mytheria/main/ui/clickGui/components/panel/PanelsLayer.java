package ru.mytheria.main.ui.clickGui.components.panel;


import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.module.Category;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.api.util.math.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PanelsLayer extends Component {

    List<PanelComponent> componentsList = new ArrayList<>();

    @Override
    public void init() {
        componentsList.clear();
        Arrays.stream(Category.values()).map(PanelComponent::new).forEach(componentsList::add);
        initModules();
    }

    public void initModules() {
        componentsList.forEach(PanelComponent::init);
    }

    @Override
    public PanelsLayer render(DrawContext context, int mouseX, int mouseY, float delta) {
        AtomicReference<Float> offset = new AtomicReference<>(0f);
        componentsList.forEach(e -> {
            e.position(getX() + offset.get(), getY()).size(250f / 2, getHeight()).render(context, mouseX, mouseY, delta);

            offset.set(offset.get() + 250 / 2 + 5);
        });

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()))
            componentsList.forEach(e -> e.mouseClicked(mouseX, mouseY, button));

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        componentsList.forEach(e -> e.mouseReleased(mouseX, mouseY, button));

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        componentsList.forEach(e -> e.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        componentsList.forEach(e -> e.keyPressed(keyCode, scanCode, modifiers));

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        componentsList.forEach(e -> e.keyReleased(keyCode, scanCode, modifiers));

        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
