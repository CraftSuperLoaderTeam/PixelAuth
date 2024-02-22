package io.pixel.network.packet.login;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerLoginClient;
import io.pixel.network.packet.Packet;
import io.pixel.util.CryptManager;

import java.io.IOException;
import java.security.PublicKey;

public class PacketEncryptionRequest implements Packet<INetHandlerLoginClient> {
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


    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleEncryptionRequest(this);
    }
}
