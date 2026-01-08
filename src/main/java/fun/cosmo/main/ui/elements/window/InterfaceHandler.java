package fun.cosmo.main.ui.elements.window;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.module.settings.impl.BooleanSetting;
import fun.cosmo.api.util.render.RenderEngine;
import fun.cosmo.api.util.shader.common.states.ColorState;
import fun.cosmo.api.util.shader.common.states.RadiusState;
import fun.cosmo.main.ui.elements.window.components.BooleanComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InterfaceHandler {
    private float x, y, width, height;
    private final List<Setting> settings;
    private final List<InterfaceWindow> components = new ArrayList<>();

    public InterfaceHandler(float x, float y, float width, float height, List<Setting> settings) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.settings = settings;
        for (Setting setting : settings) {
            if (setting instanceof BooleanSetting) components.add(new BooleanComponent(setting.getName(), ((BooleanSetting) setting)));
          //  else if (setting instanceof ListSetting) components.add(new ListComponent(setting.getName(), ((ListSetting) setting)));
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        RenderEngine.startScissor(
                context,
                getX() + (getWidth() - getWidth()) / 2,
                getY() + (getFinalHeight() - getFinalHeight()) / 2,
                getWidth(),
                getFinalHeight()
        );


        RenderEngine.drawBlurRectangle(                context.getMatrices(),
                getX() + (getWidth() - getWidth()) / 2,
                getY() + (getFinalHeight() - getFinalHeight() ) / 2,
                getWidth(),
                getFinalHeight(), new ColorState(new Color(255, 255, 255)), new RadiusState(2f), 1, 15,7f);

        float finalY = y;

        for (InterfaceWindow component : components) {
            component.setX(x);
            component.setY(finalY);
            component.setWidth(width);
            component.setHeight(height);
            component.render(context, mouseX, mouseY, 0);
            finalY += component.getHeight() + 4.5f;
        }

        RenderEngine.stopScissor(context);
    }

    public void reset() {
      //  animation.update(false);
    }

    public void closed() {
       return;
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        for (InterfaceWindow component : components) component.mouseClicked(mouseX, mouseY, button);
    }

    public float getFinalHeight() {
        float height = 0;
        for (InterfaceWindow component : components) {
            if (!component.getVisible().get()) continue;
            height += component.getHeight() + component.getAddHeight().get() + 4.5f;
        }

        return height;
    }
}