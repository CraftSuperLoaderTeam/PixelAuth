package io.pixel.network.packet.login;

import io.pixel.network.PacketBuffer;
import io.pixel.network.handle.INetHandlerLoginClient;
import io.pixel.network.packet.Packet;

import java.io.IOException;

public class PacketEnableCompression implements Packet<INetHandlerLoginClient> {
    private int compressionThreshold;

    public PacketEnableCompression() {
    }

    public PacketEnableCompression(int thresholdIn) {
        this.compressionThreshold = thresholdIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.compressionThreshold = buf.readVarIntFromBuffer();
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(this.compressionThreshold);
    }


    public void processPacket(INetHandlerLoginClient handler) {
        handler.handleEnableCompression(this);
    }
}
