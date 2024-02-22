package io.pixel.pcall.network.packet.stats;

import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.IStatusServer;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;

public class PacketServerQuery implements Packet<IStatusServer> {
    public void readPacketData(PacketBuffer buf) throws IOException {
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
    }

    public void processPacket(IStatusServer handler) {
        handler.processServerQuery(this);
    }
}
