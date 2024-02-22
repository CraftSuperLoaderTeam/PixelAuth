package io.pixel.pcall.network.packet;

import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.IHandshakeServer;

import java.io.IOException;

public class ClientHandshake implements Packet<IHandshakeServer>{
    private int protocolVersion;
    private String ip;
    private int port;
    private PacketIndex requestedState;


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.protocolVersion = buf.readVarIntFromBuffer();
        this.ip = buf.readStringFromBuffer(255);
        this.port = buf.readUnsignedShort();
        this.requestedState = PacketIndex.getById(buf.readVarIntFromBuffer());
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.protocolVersion);
        buf.writeString(this.ip);
        buf.writeShort(this.port);
        buf.writeVarIntToBuffer(this.requestedState.getId());
    }


    public void processPacket(IHandshakeServer handler) {
        handler.processHandshake(this);
    }

    public PacketIndex getRequestedState() {
        return this.requestedState;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }
}
