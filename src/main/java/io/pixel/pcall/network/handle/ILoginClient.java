package io.pixel.pcall.network.handle;

import io.pixel.pcall.network.packet.login.PacketEnableCompression;
import io.pixel.pcall.network.packet.login.PacketLoginSuccess;
import io.pixel.pcall.network.packet.login.PacketDisconnect;
import io.pixel.pcall.network.packet.login.PacketEncryptionRequest;

public interface ILoginClient extends INetHandler{
    void handleEncryptionRequest(PacketEncryptionRequest packetIn);

    void handleLoginSuccess(PacketLoginSuccess packetIn);

    void handleDisconnect(PacketDisconnect packetIn);

    void handleEnableCompression(PacketEnableCompression packetIn);
}
