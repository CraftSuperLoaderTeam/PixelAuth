package io.pixel.network.packet.stats;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerStatusServer;
import io.pixel.network.packet.Packet;

import java.io.IOException;

public class PacketServerQuery implements Packet<INetHandlerStatusServer> {
    public void readPacketData(PacketBuffer buf) throws IOException {
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
    }

    public void processPacket(INetHandlerStatusServer handler) {
        handler.processServerQuery(this);
    }
}
