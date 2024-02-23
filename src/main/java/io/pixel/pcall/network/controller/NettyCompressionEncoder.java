package io.pixel.pcall.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.pixel.pcall.network.PacketBuffer;

import java.util.zip.Deflater;

public class NettyCompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final byte[] buffer = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public NettyCompressionEncoder(int thresholdIn) {
        this.threshold = thresholdIn;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext context, ByteBuf buf1, ByteBuf buf2) throws Exception {
        int i = buf1.readableBytes();
        PacketBuffer packetbuffer = new PacketBuffer(buf2);

        if (i < this.threshold) {
            packetbuffer.writeVarIntToBuffer(0);
            packetbuffer.writeBytes(buf1);
        } else {
            byte[] abyte = new byte[i];
            buf1.readBytes(abyte);
            packetbuffer.writeVarIntToBuffer(abyte.length);
            this.deflater.setInput(abyte, 0, i);
            this.deflater.finish();

            while (!this.deflater.finished()) {
                int j = this.deflater.deflate(this.buffer);
                packetbuffer.writeBytes(this.buffer, 0, j);
            }

            this.deflater.reset();
        }
    }

    public void setCompressionThreshold(int thresholdIn) {
        this.threshold = thresholdIn;
    }
}
