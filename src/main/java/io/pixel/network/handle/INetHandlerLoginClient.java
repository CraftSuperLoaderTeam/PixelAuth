package io.pixel.network.handle;

import io.pixel.network.packet.login.PacketDisconnect;
import io.pixel.network.packet.login.PacketEnableCompression;
import io.pixel.network.packet.login.PacketEncryptionRequest;
import io.pixel.network.packet.login.PacketLoginSuccess;

public interface INetHandlerLoginClient extends INetHandler{
    void handleEncryptionRequest(PacketEncryptionRequest packetIn);

    void handleLoginSuccess(PacketLoginSuccess packetIn);

    void handleDisconnect(PacketDisconnect packetIn);

    void handleEnableCompression(PacketEnableCompression packetIn);
}
