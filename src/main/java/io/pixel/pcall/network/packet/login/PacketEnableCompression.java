package io.pixel.pcall.network.packet.login;

import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.handle.ILoginClient;
import io.pixel.pcall.network.packet.Packet;

import java.io.IOException;

public class PacketEnableCompression implements Packet<ILoginClient> {
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


    public void processPacket(ILoginClient handler) {
        handler.handleEnableCompression(this);
    }
}
