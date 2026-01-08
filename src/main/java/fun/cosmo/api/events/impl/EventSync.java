package fun.cosmo.api.events.impl;

import net.minecraft.client.network.ClientPlayerEntity;
import fun.cosmo.api.events.Event;

public class EventSync extends Event {
    public final ClientPlayerEntity player;

    public EventSync(ClientPlayerEntity player) {
        this.player = player;
    }
}
