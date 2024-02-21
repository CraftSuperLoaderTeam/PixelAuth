package io.pixel.network.packet;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandler;

import java.io.IOException;

public interface Packet<T extends INetHandler> {
    void readPacketData(PacketBuffer buf) throws IOException;


    void writePacketData(PacketBuffer buf) throws IOException;


    void processPacket(T handler);
}
