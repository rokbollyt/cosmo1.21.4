package fun.cosmo.api.client.managers;

import fun.cosmo.main.module.render.*;
import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import fun.cosmo.Mytheria;
import fun.cosmo.api.clientannotation.QuickImport;
import fun.cosmo.api.events.impl.KeyEvent;
import fun.cosmo.api.events.impl.ModuleEvent;
import fun.cosmo.api.events.impl.TickEvent;
import fun.cosmo.api.module.Module;
import fun.cosmo.main.module.combat.AttackAura;
import fun.cosmo.main.module.misc.Unhook;
import fun.cosmo.main.module.movement.SlimeBooster;
import fun.cosmo.main.module.movement.Sprint;
import fun.cosmo.main.module.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public final class ModuleManager implements QuickImport {

    List<Module> moduleLayers = new ArrayList<>();

    public ModuleManager() {
        Mytheria.getInstance().getEventProvider().subscribe(this);
        // Регистрируем публикацию TickEvent
        registerTickPublisher();
    }

    // ВАЖНО: Этот метод публикует TickEvent каждый тик
    private void registerTickPublisher() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Публикуем TickEvent в шину событий
            Mytheria.getInstance().getEventProvider().post(new TickEvent());
        });
    }

    public void init() {
        moduleLayers.addAll(
                List.of(
                        new Interface(),
                        new TargetESP(),
                        new Unhook(),
                        new FullBright(),
                        new Sprint(),
                        new SeeInvisible(),
                        new WorldTweaks(),
                        new NoRender(),
                        new HitColor(),
                        new SlimeBooster(),
                        new AttackAura()
                )
        );

        moduleLayers.forEach(Mytheria.getInstance().getEventProvider()::subscribe);
    }

    // Метод для получения модуля по имени
    public Module getModule(String name) {
        for (Module module : moduleLayers) {
            if (module.getModuleName().getString().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public Module find(Class<? extends Module> clazz) {
        return moduleLayers.stream()
                .filter(e -> e.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    public List<Module> filter(Predicate<Module> predicate) {
        return moduleLayers.stream()
                .filter(predicate)
                .toList();
    }

    public void forEach(Consumer<Module> action) {
        moduleLayers.forEach(action);
    }

    @EventHandler
    private void keyEventListener(KeyEvent keyEvent) {
        if (Unhook.ACTIVE) return;

        moduleLayers.forEach(e -> {
            if (keyEvent.getKey() == e.getKey()
                    && keyEvent.getAction() == 1
                    && mc.currentScreen == null) {
                e.toggleEnabled();
            }
        });
    }

    @EventHandler
    private void toggleEventListener(ModuleEvent.ToggleEvent toggleEvent) {
        moduleLayers.forEach(e -> {
            if (toggleEvent.getModuleLayer().equals(e))
                e.toggleEnabled();
        });
    }
}