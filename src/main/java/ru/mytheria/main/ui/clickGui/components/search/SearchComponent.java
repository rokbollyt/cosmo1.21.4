package ru.mytheria.main.ui.clickGui.components.search;

import com.google.common.base.Suppliers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import ru.mytheria.Mytheria;
import ru.mytheria.api.client.localization.Localization;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.util.color.ColorUtil;
import ru.mytheria.api.util.render.ScissorUtil;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.main.ui.clickGui.Component;
import ru.mytheria.api.util.math.Math;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchComponent extends Component {

  /*  @Getter
    static Supplier<SearchSource> searchSource = Suppliers.memoize(() -> new SearchSource(Localization.get("cp.search"), Mytheria.getInstance().getClickGuiScreen().getPanelsLayer()::initModules));*/
    @Getter
    static Supplier<SearchSource> searchSource = () -> new SearchSource(Localization.get("cp.search"), Mytheria.getInstance().getClickGuiScreen().getPanelsLayer()::initModules);

    @Override
    public SearchComponent render(DrawContext context, int mouseX, int mouseY, float delta) {
        QuickApi.blur()
                .size(new SizeState(getWidth(), getHeight()))
                .radius(new RadiusState(5))
                .blurRadius(16)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.rectangle()
                .radius(new RadiusState(5))
                .color(new ColorState(ColorUtil.applyOpacity(0xFF000000, searchSource.get().isSelected() ? 40 : 20)))
                .size(new SizeState(getWidth(), getHeight()))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        QuickApi.border()
                .radius(new RadiusState(5))
                .color(new ColorState(ColorUtil.applyOpacity(-1, searchSource.get().isSelected() ? 30 : 10)))
                .thickness(-.5f)
                .size(new SizeState(getWidth(), getHeight()))
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX(), getY());

        String text = searchSource.get().isSelected() && !searchSource.get().getText().isEmpty() ? searchSource.get().getText().toString() : searchSource.get().getDefaultText();

        ScissorUtil.push(getX(), getY(), getWidth(), getHeight());
        QuickApi.text()
                .color(-1)
                .font(QuickApi.inter())
                .text(text)
                .size(8)
                .build()
                .render(context.getMatrices().peek().getPositionMatrix(), getX() + 5, getY() + getHeight() / 4);
        ScissorUtil.pop();

        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Math.isHover(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            searchSource.get().toggle();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        searchSource.get().keyPressed(keyCode);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        searchSource.get().charTyped(chr);

        return super.charTyped(chr, modifiers);
    }
}
