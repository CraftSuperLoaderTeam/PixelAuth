package io.pixel.network.packet.login;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerLoginServer;
import io.pixel.network.packet.Packet;
import io.pixel.util.CryptManager;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.PrivateKey;

public class PacketEncryptionResponse implements Packet<INetHandlerLoginServer> {
    private byte[] secretKeyEncrypted = new byte[0];
    private byte[] verifyTokenEncrypted = new byte[0];


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.secretKeyEncrypted = buf.readByteArray();
        this.verifyTokenEncrypted = buf.readByteArray();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByteArray(this.secretKeyEncrypted);
        buf.writeByteArray(this.verifyTokenEncrypted);
    }


    public void processPacket(INetHandlerLoginServer handler) {
        handler.processEncryptionResponse(this);
    }

    public SecretKey getSecretKey(PrivateKey key) {
        return CryptManager.decryptSharedKey(key, this.secretKeyEncrypted);
    }

    public byte[] getVerifyToken(PrivateKey key) {
        return key == null ? this.verifyTokenEncrypted : CryptManager.decryptData(key, this.verifyTokenEncrypted);
    }
}
