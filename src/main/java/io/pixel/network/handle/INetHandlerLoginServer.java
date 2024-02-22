package io.pixel.network.handle;

import io.pixel.network.packet.login.PacketEncryptionResponse;
import io.pixel.network.packet.login.PacketLoginStart;

public interface INetHandlerLoginServer extends INetHandler{
    void processLoginStart(PacketLoginStart packetIn);

    void processEncryptionResponse(PacketEncryptionResponse packetIn);
}
