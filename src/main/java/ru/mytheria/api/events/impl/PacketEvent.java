package ru.mytheria.api.events.impl;


import ru.mytheria.api.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.packet.Packet;

@Getter
@AllArgsConstructor
public class PacketEvent extends Event {

    Packet<?> packet;
    PacketEventType packetEventType;

    public enum PacketEventType {
        SEND,
        RECEIVE
    }
}
