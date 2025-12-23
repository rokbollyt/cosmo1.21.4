package ru.mytheria;

import lombok.Getter;
import lombok.experimental.NonFinal;
import net.fabricmc.api.ModInitializer;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;

import ru.mytheria.api.client.ChatCommandHook;
import ru.mytheria.api.client.KeyboardInputHook;
import ru.mytheria.api.client.configuration.AutoConfigTask;
import ru.mytheria.api.client.configuration.ConfigurationService;
import ru.mytheria.api.client.draggable.data.DraggableRepository;
import ru.mytheria.api.client.localization.Language;
import ru.mytheria.api.client.managers.ModuleManager;
import ru.mytheria.api.util.enviorement.ServerManager;
import ru.mytheria.api.util.media.MediaPlayer;
import ru.mytheria.main.ui.clickGui.ClickGuiScreen;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Mytheria implements ModInitializer {

    @Getter
    public static Mytheria instance;
    @Getter
    public static IEventBus eventProvider;
    @Getter
    public ModuleManager moduleManager;
    @Getter
    public ClickGuiScreen clickGuiScreen;
    @Getter
    public DraggableRepository draggableRepository;
    @Getter
    private MediaPlayer mediaPlayer;
    @Getter
    ConfigurationService configurationService;
    @Getter
    @NonFinal
    Language language = Language.ENG;
    @Getter
    private ServerManager serverManager;

    @Override
    public void onInitialize() {
        instance = this;
        eventProvider = new EventBus();
        eventProvider.registerLambdaFactory(
                "ru.mytheria",
                ( Method method, Class<?> clazz) -> MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
        );
        KeyboardInputHook.init();

        AutoConfigTask.init();
        ChatCommandHook.init();

        mediaPlayer = new MediaPlayer();
        moduleManager = new ModuleManager();
        serverManager = new ServerManager();
        this.draggableRepository = new DraggableRepository();

        this.draggableRepository.init();
        this.moduleManager.init();
        this.configurationService = new ConfigurationService();
        this.clickGuiScreen = new ClickGuiScreen();

        this.configurationService.save("autosave");
        this.configurationService.load("autosave");
    }

    public void lngEng() {
        language = Language.ENG;
    }

    public void lngRus() {
        language = Language.RUS;
    }

    public void toggleLanguage() {
        if (language == Language.ENG) {
            language = Language.RUS;
            System.out.println("Language switched to RUS");
        } else {
            language = Language.ENG;
            System.out.println("Language switched to ENG");
        }
    }

}
