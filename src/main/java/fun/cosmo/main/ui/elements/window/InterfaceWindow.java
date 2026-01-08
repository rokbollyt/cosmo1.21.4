package fun.cosmo.main.ui.elements.window;


import lombok.Getter;
import lombok.Setter;
import fun.cosmo.main.ui.elements.window.components.Component;

@Getter @Setter
public abstract class InterfaceWindow extends Component {

    public InterfaceWindow(String name) {
        super(name);
    }
}