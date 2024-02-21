package io.pixel.network.packet.stats;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerStatusServer;
import io.pixel.network.packet.Packet;

import java.io.IOException;

public class PacketPing implements Packet<INetHandlerStatusServer> {
    private long clientTime;


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.clientTime = buf.readLong();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.clientTime);
    }


    public void processPacket(INetHandlerStatusServer handler) {
        handler.processPing(this);
    }

    public long getClientTime() {
        return this.clientTime;
    }
}
