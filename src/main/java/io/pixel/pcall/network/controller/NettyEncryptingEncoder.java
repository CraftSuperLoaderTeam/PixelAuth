package io.pixel.pcall.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.pixel.pcall.network.NettyEncryptionTranslator;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class NettyEncryptingEncoder extends MessageToByteEncoder<ByteBuf> {
    private final NettyEncryptionTranslator encryptionCodec;

    public NettyEncryptingEncoder(Cipher cipher) {
        this.encryptionCodec = new NettyEncryptionTranslator(cipher);
    }

    protected void encode(ChannelHandlerContext context, ByteBuf byteBuf, ByteBuf byteBuf1) throws ShortBufferException, Exception {
        this.encryptionCodec.cipher(byteBuf, byteBuf1);
    }
}
