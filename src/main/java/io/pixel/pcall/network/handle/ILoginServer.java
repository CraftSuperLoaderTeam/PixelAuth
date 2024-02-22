package io.pixel.pcall.network.handle;

import io.pixel.pcall.network.packet.login.PacketEncryptionResponse;
import io.pixel.pcall.network.packet.login.PacketLoginStart;

public interface ILoginServer extends INetHandler{
    void processLoginStart(PacketLoginStart packetIn);

    void processEncryptionResponse(PacketEncryptionResponse packetIn);
}
