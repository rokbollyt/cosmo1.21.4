package fun.cosmo.api.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundUtil {

    // --- 1. ОПРЕДЕЛЕНИЕ ИДЕНТИФИКАТОРОВ ЗВУКОВ ---
    // Название должно совпадать с названием файла (без расширения .ogg)
    // Domain: "mre" (ваше название ресурса)
    // Path: "sounds/" + "swipe" / "bind_set"

    // Звук для переключения настроек (Boolean, Mode, Slider)
    private static final Identifier SWIPE_ID = Identifier.of("mre", "swipe");

    // Звук для установки бинда
    private static final Identifier BIND_SET_ID = Identifier.of("mre", "bind_set");

    // --- 2. РЕГИСТРАЦИЯ SOUNDEVENT (ВАЖНО! Этот код должен быть в вашем главном классе/модуле) ---
    /*
     * ВНИМАНИЕ: Для корректной работы в Minecraft, SoundEvent'ы должны быть зарегистрированы
     * в реестре SoundEvent во время инициализации мода.
     * Например, в вашем главном классе Mytheria:
     * * public static final SoundEvent SWIPE_SOUND_EVENT = SoundEvent.of(SWIPE_ID);
     * public static final SoundEvent BIND_SET_SOUND_EVENT = SoundEvent.of(BIND_SET_ID);
     * * Registry.register(Registries.SOUND_EVENT, SWIPE_ID, SWIPE_SOUND_EVENT);
     * Registry.register(Registries.SOUND_EVENT, BIND_SET_ID, BIND_SET_SOUND_EVENT);
     */

    // --- 3. МЕТОД ВОСПРОИЗВЕДЕНИЯ ЗВУКА ---

    /**
     * Воспроизводит пользовательский звук 'swipe.ogg' из ресурсов MRE.
     */
    public static void playSwipeSound() {
        // Мы используем Identifier, даже если он не зарегистрирован как SoundEvent,
        // для воспроизведения пользовательского звука через PositionedSoundInstance.
        // Но лучше использовать зарегистрированный SoundEvent, если это возможно в вашем Mod API.

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.getSoundManager() != null) {

            // Если вы используете SoundEvent (РЕКОМЕНДУЕТСЯ):
            // mc.getSoundManager().play(PositionedSoundInstance.master(Mytheria.SWIPE_SOUND_EVENT, 1.0f));

            // Если вы используете только Identifier (требует, чтобы звук был в файле sounds.json):
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(SWIPE_ID), 1.0f, 0.5f));
        }
    }

    /**
     * Воспроизводит пользовательский звук для установки бинда.
     */
    public static void playBindSetSound() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.getSoundManager() != null) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvent.of(BIND_SET_ID), 1.0f, 0.5f));
        }
    }
}