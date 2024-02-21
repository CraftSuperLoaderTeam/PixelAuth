package io.pixel.network.packet.stats;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerStatusClient;
import io.pixel.network.packet.Packet;

import java.io.IOException;

public class PacketPong implements Packet<INetHandlerStatusClient> {
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


    public void processPacket(INetHandlerStatusClient handler) {
        handler.handlePong(this);
    }
}
