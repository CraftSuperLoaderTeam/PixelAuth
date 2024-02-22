package io.pixel.pcall.network.packet;

import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.INetHandler;

import java.io.IOException;

public interface Packet<T extends INetHandler> {
    void readPacketData(PacketBuffer buf) throws IOException;


    void writePacketData(PacketBuffer buf) throws IOException;


    void processPacket(T handler);
}
