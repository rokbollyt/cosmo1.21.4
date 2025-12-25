package ru.mytheria.api.client.managers;

import lombok.Getter;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.api.events.impl.KeyEvent;
import ru.mytheria.api.events.impl.ModuleEvent;
import ru.mytheria.api.events.impl.TickEvent;
import ru.mytheria.api.module.Module;
import ru.mytheria.main.module.combat.AttackAura;
import ru.mytheria.main.module.misc.Unhook;
import ru.mytheria.main.module.movement.FixMovement;
import ru.mytheria.main.module.movement.Sprint;
import ru.mytheria.main.module.render.FullBright;
import ru.mytheria.main.module.render.Interface;
import ru.mytheria.main.module.render.TargetESP;

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
                        new FixMovement(),
                        new AttackAura()
                )
        );

        moduleLayers.forEach(Mytheria.getInstance().getEventProvider()::subscribe);
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