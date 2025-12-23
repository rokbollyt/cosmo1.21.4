package ru.mytheria.api.events.impl;

import net.minecraft.client.network.ClientPlayerEntity;
import ru.mytheria.api.events.Event;

public class EventSync extends Event {
    public final ClientPlayerEntity player;

    public EventSync(ClientPlayerEntity player) {
        this.player = player;
    }
}
