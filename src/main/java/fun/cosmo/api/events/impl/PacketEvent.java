package fun.cosmo.api.events.impl;


import fun.cosmo.api.events.Event;
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
