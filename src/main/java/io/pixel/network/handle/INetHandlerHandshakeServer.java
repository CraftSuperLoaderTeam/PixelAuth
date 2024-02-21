package io.pixel.network.handle;

import io.pixel.network.packet.ClientHandshake;

public interface INetHandlerHandshakeServer extends INetHandler{
    void processHandshake(ClientHandshake packetIn);
}
