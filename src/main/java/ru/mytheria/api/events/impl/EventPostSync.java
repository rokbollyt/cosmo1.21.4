package ru.mytheria.api.events.impl;

import net.minecraft.client.network.ClientPlayerEntity;
import ru.mytheria.api.events.Event;

public class EventPostSync extends Event {
    public final ClientPlayerEntity player;

    public EventPostSync(ClientPlayerEntity player) {
        this.player = player;
    }
}
