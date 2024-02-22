package io.pixel.pcall.network.packet.stats;

import io.pixel.pcall.network.handle.IStatusClient;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;

public class PacketPong implements Packet<IStatusClient> {
    private long clientTime;

    public PacketPong() {
    }

    public PacketPong(long clientTimeIn) {
        this.clientTime = clientTimeIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.clientTime = buf.readLong();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeLong(this.clientTime);
    }


    public void processPacket(IStatusClient handler) {
        handler.handlePong(this);
    }
}
