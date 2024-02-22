package io.pixel.network.packet.login;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerLoginClient;
import io.pixel.network.packet.Packet;
import io.pixel.util.text.ITextComponent;

import java.io.IOException;

public class PacketDisconnect implements Packet<INetHandlerLoginClient> {
    private ITextComponent reason;

    public PacketDisconnect() {
    }

    public PacketDisconnect(ITextComponent p_i46853_1_) {
        this.reason = p_i46853_1_;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.reason = ITextComponent.Serializer.fromJsonLenient(buf.readStringFromBuffer(32767));
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeTextComponent(this.reason);
    }


    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleDisconnect(this);
    }
}
