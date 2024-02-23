package io.pixel.pcall.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.pixel.pcall.network.PacketBuffer;

import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class NettyCompressionDecoder extends ByteToMessageDecoder {
    private final Inflater inflater;
    private int threshold;

    public NettyCompressionDecoder(int thresholdIn) {
        this.threshold = thresholdIn;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext context, ByteBuf buf1, List<Object> list) throws DataFormatException, Exception {
        if (buf1.readableBytes() != 0) {
            PacketBuffer packetbuffer = new PacketBuffer(buf1);
            int i = packetbuffer.readVarIntFromBuffer();

            if (i == 0) {
                list.add(packetbuffer.readBytes(packetbuffer.readableBytes()));
            } else {
                if (i < this.threshold) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
                }

                if (i > 2097152) {
                    throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of " + 2097152);
                }

                byte[] abyte = new byte[packetbuffer.readableBytes()];
                packetbuffer.readBytes(abyte);
                this.inflater.setInput(abyte);
                byte[] abyte1 = new byte[i];
                this.inflater.inflate(abyte1);
                list.add(Unpooled.wrappedBuffer(abyte1));
                this.inflater.reset();
            }
        }
    }

    public void setCompressionThreshold(int thresholdIn) {
        this.threshold = thresholdIn;
    }
}
