package io.pixel.pcall.network.handle;

import io.pixel.pcall.network.packet.stats.PacketPong;
import io.pixel.pcall.network.packet.stats.PacketServerInfo;

public interface IStatusClient extends INetHandler{
    void handleServerInfo(PacketServerInfo packetIn);

    void handlePong(PacketPong packetIn);
}
