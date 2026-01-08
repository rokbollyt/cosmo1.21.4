package fun.cosmo.mixin;

import fun.cosmo.api.events.EventManager;
import fun.cosmo.api.events.impl.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "send", at = @At("HEAD"))
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        EventManager.call(new PacketEvent(packet, PacketEvent.PacketEventType.SEND));
    }

    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        EventManager.call(new PacketEvent(packet, PacketEvent.PacketEventType.RECEIVE));
    }
}
