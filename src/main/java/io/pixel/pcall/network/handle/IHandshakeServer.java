package io.pixel.pcall.network.handle;

import io.pixel.pcall.network.packet.ClientHandshake;

public interface IHandshakeServer extends INetHandler{
    void processHandshake(ClientHandshake packetIn);
}
