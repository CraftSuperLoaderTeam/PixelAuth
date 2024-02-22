package io.pixel.pcall.network.packet.login;

import io.pixel.pcall.network.handle.ILoginClient;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.packet.Packet;
import io.pixel.pcall.util.CryptManager;

import java.io.IOException;
import java.security.PublicKey;

public class PacketEncryptionRequest implements Packet<ILoginClient> {
    private String hashedServerId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    public PacketEncryptionRequest() {
    }

    public PacketEncryptionRequest(String serverIdIn, PublicKey publicKeyIn, byte[] verifyTokenIn) {
        this.hashedServerId = serverIdIn;
        this.publicKey = publicKeyIn;
        this.verifyToken = verifyTokenIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.hashedServerId = buf.readStringFromBuffer(20);
        this.publicKey = CryptManager.decodePublicKey(buf.readByteArray());
        this.verifyToken = buf.readByteArray();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(this.hashedServerId);
        buf.writeByteArray(this.publicKey.getEncoded());
        buf.writeByteArray(this.verifyToken);
    }


    public void processPacket(ILoginClient handler) {
        handler.handleEncryptionRequest(this);
    }
}
