package io.pixel.network.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
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
import java.util.List;

public class NettyPacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker RECEIVED_PACKET_MARKER = MarkerManager.getMarker("PACKET_RECEIVED", NetworkServer.NETWORK_PACKETS_MARKER);
    private final PacketDirection direction;

    public NettyPacketDecoder(PacketDirection direction) {
        this.direction = direction;
    }

    protected void decode(ChannelHandlerContext p_decode_1_, ByteBuf p_decode_2_, List<Object> p_decode_3_) throws IOException, InstantiationException, IllegalAccessException, Exception {
        if (p_decode_2_.readableBytes() != 0) {
            PacketBuffer packetbuffer = new PacketBuffer(p_decode_2_);
            int i = packetbuffer.readVarIntFromBuffer();
            Packet<?> packet = ((PacketIndex) p_decode_1_.channel().attr(NetworkServer.PROTOCOL_ATTRIBUTE_KEY).get()).getPacket(this.direction, i);

            if (packet == null) {
                throw new IOException("Bad packet id " + i);
            } else {
                packet.readPacketData(packetbuffer);

                if (packetbuffer.readableBytes() > 0) {
                    throw new IOException("Packet " + ((PacketIndex) p_decode_1_.channel().attr(NetworkServer.PROTOCOL_ATTRIBUTE_KEY).get()).getId() + "/" + i + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + packetbuffer.readableBytes() + " bytes extra whilst reading packet " + i);
                } else {
                    p_decode_3_.add(packet);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(RECEIVED_PACKET_MARKER, " IN: [{}:{}] {}", p_decode_1_.channel().attr(NetworkServer.PROTOCOL_ATTRIBUTE_KEY).get(), Integer.valueOf(i), packet.getClass().getName());
                    }
                }
            }
        }
    }
}
