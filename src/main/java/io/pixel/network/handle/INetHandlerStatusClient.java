package io.pixel.network.handle;

import io.pixel.network.packet.stats.PacketPong;
import io.pixel.network.packet.stats.PacketServerInfo;

public interface INetHandlerStatusClient extends INetHandler{
    void handleServerInfo(PacketServerInfo packetIn);

    void handlePong(PacketPong packetIn);
}
