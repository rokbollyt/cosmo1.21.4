package ru.mytheria.main.ui.clickGui.components.panel;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.util.render.ScissorUtil;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.main.ui.clickGui.Helper;
import ru.mytheria.main.ui.clickGui.components.BackgroundComponent;
import ru.mytheria.main.ui.clickGui.components.module.ModuleComponent;
import ru.mytheria.main.ui.clickGui.components.search.SearchComponent;
import ru.mytheria.api.util.math.Math;
import ru.mytheria.main.ui.clickGui.components.settings.SettingComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PanelComponent extends Component {

    @NonFinal
    List<ModuleComponent> componentsList = new ArrayList<>();

    BackgroundComponent backgroundComponent;

    @NonFinal
    float scroll, animationScroll = 0f;

    Category category;

    public PanelComponent(Category category) {
        this.category = category;
        backgroundComponent = new BackgroundComponent(category);

        init();
    }

    @Override
    public void init() {
        componentsList = Helper.moduleLayers(category, ( e) -> e.getModuleName().getString().toLowerCase().startsWith(SearchComponent.getSearchSource().get().getText().toString().toLowerCase()));
    }

    @Override
    public PanelComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        animationScroll = MathHelper.lerp(.02f, animationScroll, scroll);

        backgroundComponent.position(getX(), getY()).size(getWidth(), getHeight()).render(context, mouseX, mouseY, delta);

        ScissorUtil.push(getX() + 2.5f, getY() + 32, getWidth() - 5, getHeight() - 32 - 14.5f);

        AtomicReference<Float> offset = new AtomicReference<>(0f);
        componentsList.forEach(e -> {
            e.getComponents().forEach(SettingComponent::init);

            float height = e.getHeight();

            e.position(getX() + 2.5f, getY() + 32 + offset.get() + animationScroll)
                    .size(240f / 2, height)
                    .render(context, mouseX, mouseY, delta);

            offset.set(offset.get() + height + 2.5f);
        });

        ScissorUtil.pop();

        scroll = clamp(scroll, min(getHeight() - offset.get() - 45.5f, 0), 0);

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
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight()))
            scroll += verticalAmount * 10f;

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
