package io.pixel.pcall.network.packet.login;

import com.mojang.authlib.GameProfile;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.ILoginServer;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;
import java.util.UUID;

public class PacketLoginStart implements Packet<ILoginServer> {
    private GameProfile profile;

    public PacketLoginStart() {
    }

    public PacketLoginStart(GameProfile profileIn) {
        this.profile = profileIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.profile = new GameProfile((UUID) null, buf.readStringFromBuffer(16));
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.profile.getName());
    }


    public void processPacket(ILoginServer handler) {
        handler.processLoginStart(this);
    }

    public GameProfile getProfile() {
        return this.profile;
    }
}
