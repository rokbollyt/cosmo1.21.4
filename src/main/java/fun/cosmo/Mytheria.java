package fun.cosmo;

import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;

import fun.cosmo.api.client.KeyboardInputHook;
import fun.cosmo.api.client.configuration.AutoConfigTask;
import fun.cosmo.api.client.configuration.ConfigurationService;
import fun.cosmo.api.client.draggable.data.DraggableRepository;
import fun.cosmo.api.client.managers.ModuleManager;
import fun.cosmo.api.util.enviorement.ServerManager;
import fun.cosmo.api.util.media.MediaPlayer;
import fun.cosmo.main.ui.clickGui.ClickGuiScreen;

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
    private ServerManager serverManager;

    @Override
    public void onInitialize() {
        instance = this;
        eventProvider = new EventBus();
        eventProvider.registerLambdaFactory(
                "fun.cosmo",
                ( Method method, Class<?> clazz) -> MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
        );
        KeyboardInputHook.init();

        AutoConfigTask.init();

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


}