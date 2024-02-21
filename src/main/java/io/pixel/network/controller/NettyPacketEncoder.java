package io.pixel.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.pixel.network.NetworkServer;
import io.pixel.network.PacketBuffer;
import io.pixel.network.packet.Packet;
import io.pixel.network.packet.PacketDirection;
import io.pixel.network.packet.PacketIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;

public class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_SENT", NetworkServer.NETWORK_PACKETS_MARKER);
    private final PacketDirection direction;

    public NettyPacketEncoder(PacketDirection direction) {
        this.direction = direction;
    }

    protected void encode(ChannelHandlerContext p_encode_1_, Packet<?> p_encode_2_, ByteBuf p_encode_3_) throws IOException, Exception {
        PacketIndex enumconnectionstate = (PacketIndex) p_encode_1_.channel().attr(NetworkServer.PROTOCOL_ATTRIBUTE_KEY).get();

        if (enumconnectionstate == null) {
            throw new RuntimeException("ConnectionProtocol unknown: " + p_encode_2_.toString());
        } else {
            Integer integer = enumconnectionstate.getPacketId(this.direction, p_encode_2_);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(RECEIVED_PACKET_MARKER, "OUT: [{}:{}] {}", p_encode_1_.channel().attr(NetworkServer.PROTOCOL_ATTRIBUTE_KEY).get(), integer, p_encode_2_.getClass().getName());
            }

            if (integer == null) {
                throw new IOException("Can't serialize unregistered packet");
            } else {
                PacketBuffer packetbuffer = new PacketBuffer(p_encode_3_);
                packetbuffer.writeVarIntToBuffer(integer.intValue());

                try {
                    p_encode_2_.writePacketData(packetbuffer);
                } catch (Throwable throwable) {
                    LOGGER.error(throwable);
                }
            }
        }
    }
}
