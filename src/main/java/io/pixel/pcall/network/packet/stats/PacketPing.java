package io.pixel.pcall.network.packet.stats;

import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.IStatusServer;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;

public class PacketPing implements Packet<IStatusServer> {
    private long clientTime;


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.clientTime = buf.readLong();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.clientTime);
    }


    public void processPacket(IStatusServer handler) {
        handler.processPing(this);
    }

    public long getClientTime() {
        return this.clientTime;
    }
}
