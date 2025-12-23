package ru.mytheria.main.ui.clickGui.components.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.main.ui.clickGui.ClickGuiScreen;


@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchSource {

    String defaultText;
    Runnable runnable;

    StringBuilder text = new StringBuilder();

    @NonFinal
    boolean selected = false;

    public void toggle() {
        this.selected = !this.selected;
    }

    public void keyPressed(int key) {
        if (key == GLFW.GLFW_KEY_F && ClickGuiScreen.hasControlDown()) {
            toggle();
        }

        if (!selected || text.isEmpty()) return;

        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            this.text.delete(this.text.length() - 1, this.text.length());
            this.runnable.run();
        }
    }

    public void charTyped(char chr) {
        if (!selected) return;

        this.text.append(chr);
        this.runnable.run();
    }

}
