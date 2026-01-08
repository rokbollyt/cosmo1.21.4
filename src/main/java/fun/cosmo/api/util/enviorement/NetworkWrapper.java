package fun.cosmo.api.util.enviorement;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.network.packet.Packet;
import fun.cosmo.api.clientannotation.QuickImport;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class NetworkWrapper implements QuickImport {

    @Getter private final List<Packet<?>> silentPackets = new ArrayList<>();

    public void sendSilentPacket(Packet<?> packet) {
        silentPackets.add(packet);
        mc.getNetworkHandler().sendPacket(packet);
    }

    public void sendPacket(Packet<?> packet) {
        mc.getNetworkHandler().sendPacket(packet);
    }
}