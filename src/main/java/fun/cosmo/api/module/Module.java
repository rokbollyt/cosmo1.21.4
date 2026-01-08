package fun.cosmo.api.module;

import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import fun.cosmo.Mytheria;
import fun.cosmo.api.clientannotation.QuickImport;
import fun.cosmo.api.module.settings.Setting;
import fun.cosmo.api.util.animations.Animation;
import fun.cosmo.api.util.animations.Direction;
import fun.cosmo.api.util.animations.implement.DecelerateAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class Module implements QuickImport {

    // --- Кастомные Звуковые константы ---
    private static final Identifier ACTIVATE_SOUND = Identifier.of("mre", "module.enable");
    private static final Identifier DEACTIVATE_SOUND = Identifier.of("mre", "module.disable");
    // ------------------------------------

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

    /**
     * Воспроизводит звук, используя SoundManager Minecraft.
     * @param soundId Идентификатор звука (например, mre:module.enable)
     */
    private void playSound(Identifier soundId) {
        if (fullNullCheck()) return;

        try {
            // ВОЗВРАТ К PositionedSoundInstance.master, но с повышенной громкостью.
            // Это самый надежный способ для звуков GUI.
            PositionedSoundInstance sound = PositionedSoundInstance.master(
                    SoundEvent.of(soundId),
                    5.0F // Громкость 5.0F (увеличено для тестирования)
            );

            System.out.println("[SOUND DEBUG] Attempting to play: " + soundId + " at 5.0x volume.");
            mc.getSoundManager().play(sound);

        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE TO PLAY SOUND: " + soundId + ". Exception: " + e);
        }
    }

    public void activate() {
        Mytheria.getInstance().getEventProvider().subscribe(this);
        playSound(ACTIVATE_SOUND);
    }

    public void deactivate() {
        Mytheria.getInstance().getEventProvider().unsubscribe(this);
        playSound(DEACTIVATE_SOUND);
    }

    public Module addSettings(Setting... settings) {
        this.settingLayers.addAll(List.of(settings));
        return this;
    }
}