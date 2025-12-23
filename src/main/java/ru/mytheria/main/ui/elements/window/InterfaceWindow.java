package ru.mytheria.main.ui.elements.window;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import ru.mytheria.api.clientannotation.QuickApi;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.api.util.window.WindowLayer;
import ru.mytheria.main.ui.clickGui.ComponentBuilder;
import ru.mytheria.main.ui.elements.window.components.Component;

import java.awt.*;

@Getter @Setter
public abstract class InterfaceWindow extends Component {

    public InterfaceWindow(String name) {
        super(name);
    }
}