package io.pixel.pcall.network.handle;

import io.pixel.pcall.network.packet.stats.PacketPing;
import io.pixel.pcall.network.packet.stats.PacketServerQuery;

public interface IStatusServer extends INetHandler{
    void processPing(PacketPing packetIn);

    void processServerQuery(PacketServerQuery packetIn);
}
