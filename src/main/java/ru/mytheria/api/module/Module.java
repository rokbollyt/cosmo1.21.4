package ru.mytheria.api.module;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.api.events.impl.ModuleEvent;
import ru.mytheria.api.module.settings.Setting;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.util.animations.Animation;
import ru.mytheria.api.util.animations.Direction;
import ru.mytheria.api.util.animations.implement.DecelerateAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class Module implements QuickImport {

    Text moduleName;
    Text moduleDescription;
    Category category;

    @Setter
    Integer key = GLFW.GLFW_KEY_UNKNOWN;

    @Setter
    Integer action = 0;

    @Setter
    Boolean enabled = false, binding = false;

    List<Setting> settingLayers = new ArrayList<>();

    Animation animation = new DecelerateAnimation()
            .setMs(250)
            .setValue(1);

    public Module(Text moduleName, Text moduleDescription, Category category) {
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
        this.category = category;
        this.animation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public Module(Text moduleName, Category category) {
        this.moduleName = moduleName;
        this.moduleDescription = Text.of("Description missing.");
        this.category = category;
        this.animation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public void setEnabled(Boolean enabled) {
        if (enabled != this.enabled)
            toggleEnabled();
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
        this.animation.setDirection(enabled ? Direction.FORWARDS : Direction.BACKWARDS);

        if (enabled) activate();
        else deactivate();
    }
    public boolean isEnabled() {
        return enabled;
    }

    public List<Setting> filter(Predicate<Setting> predicate) {
        return settingLayers.stream().filter(predicate).toList();
    }

    public void forEach(Consumer<Setting> action) {
        settingLayers.forEach(action);
    }

    public void activate() {
        System.out.println("[DEBUG] Module " + getModuleName().getString() + " activated!");
        Mytheria.getInstance().getEventProvider().subscribe(this);
    }

    public void deactivate() {
        System.out.println("[DEBUG] Module " + getModuleName().getString() + " deactivated!");
        Mytheria.getInstance().getEventProvider().unsubscribe(this);
    }

    public Module addSettings(Setting... settings) {
        this.settingLayers.addAll(List.of(settings));
        return this;
    }

}
