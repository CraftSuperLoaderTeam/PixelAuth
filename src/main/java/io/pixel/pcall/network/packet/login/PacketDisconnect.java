package io.pixel.pcall.network.packet.login;

import io.pixel.pcall.network.handle.ILoginClient;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.packet.Packet;
import io.pixel.pcall.util.text.ITextComponent;

import java.io.IOException;

public class PacketDisconnect implements Packet<ILoginClient> {
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


    public void processPacket(ILoginClient handler) {
        handler.handleDisconnect(this);
    }
}
