package io.pixel.pcall.network.packet.login;

import com.mojang.authlib.GameProfile;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.ILoginClient;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;
import java.util.UUID;

public class PacketLoginSuccess implements Packet<ILoginClient> {
    private GameProfile profile;

    public PacketLoginSuccess() {
    }

    public PacketLoginSuccess(GameProfile profileIn) {
        this.profile = profileIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        String s = buf.readStringFromBuffer(36);
        String s1 = buf.readStringFromBuffer(16);
        UUID uuid = UUID.fromString(s);
        this.profile = new GameProfile(uuid, s1);
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        UUID uuid = this.profile.getId();
        buf.writeString(uuid == null ? "" : uuid.toString());
        buf.writeString(this.profile.getName());
    }


    public void processPacket(ILoginClient handler) {
        handler.handleLoginSuccess(this);
    }
}
