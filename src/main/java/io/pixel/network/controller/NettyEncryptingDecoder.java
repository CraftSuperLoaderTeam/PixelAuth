package io.pixel.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.pixel.network.NettyEncryptionTranslator;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;
import java.util.List;

public class NettyEncryptingDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final NettyEncryptionTranslator decryptionCodec;

    public NettyEncryptingDecoder(Cipher cipher) {
        this.decryptionCodec = new NettyEncryptionTranslator(cipher);
    }

    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws ShortBufferException, Exception {
        list.add(this.decryptionCodec.decipher(context, byteBuf));
    }
}
