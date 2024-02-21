package io.pixel.network.handle;

import io.pixel.network.packet.stats.PacketPing;
import io.pixel.network.packet.stats.PacketServerQuery;

public interface INetHandlerStatusServer extends INetHandler{
    void processPing(PacketPing packetIn);

    void processServerQuery(PacketServerQuery packetIn);
}
