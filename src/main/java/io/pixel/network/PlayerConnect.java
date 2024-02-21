package io.pixel.network;

import com.google.common.collect.Queues;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.pixel.network.handle.INetHandler;
import io.pixel.network.packet.Packet;
import io.pixel.network.packet.PacketDirection;
import io.pixel.network.packet.PacketIndex;
import io.pixel.util.ThreadQuickExitException;
import io.pixel.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.pixel.network.NetworkServer.PROTOCOL_ATTRIBUTE_KEY;


public class PlayerConnect extends SimpleChannelInboundHandler<Packet<?>> {

    private static final Logger LOGGER = LogManager.getLogger(PlayerConnect.class);
    private final PacketDirection direction;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Queue<PlayerConnect.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.<PlayerConnect.InboundHandlerTuplePacketListener>newConcurrentLinkedQueue();
    private Channel channel;
    private INetHandler packetListener;
    private ITextComponent terminationReason;
    SocketAddress socketAddress;


    public PlayerConnect(PacketDirection packetDirection) {
        this.direction = packetDirection;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        this.channel = context.channel();
        this.socketAddress = this.channel.remoteAddress();

        try {
            this.setConnectionState(PacketIndex.HANDSHAKING);
        } catch (Throwable throwable) {
            LOGGER.fatal(throwable);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Packet<?> packet) throws Exception {
        if(this.channel.isOpen()){
            try {
                ((Packet<INetHandler>) packet).processPacket(this.packetListener);
            } catch (ThreadQuickExitException var4) {
                ;
            }
        }
    }

    private void dispatchPacket(final Packet<?> inPacket,final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {
        final PacketIndex enumconnectionstate = PacketIndex.getFromPacket(inPacket);
        final PacketIndex enumconnectionstate1 = (PacketIndex) this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();

        if (enumconnectionstate1 != enumconnectionstate) {
            LOGGER.debug("Disabled auto read");
            this.channel.config().setAutoRead(false);
        }

        if (this.channel.eventLoop().inEventLoop()) {
            if (enumconnectionstate != enumconnectionstate1) {
                this.setConnectionState(enumconnectionstate);
            }

            ChannelFuture channelfuture = this.channel.writeAndFlush(inPacket);

            if (futureListeners != null) {
                channelfuture.addListeners(futureListeners);
            }

            channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            this.channel.eventLoop().execute(new Runnable() {
                public void run() {
                    if (enumconnectionstate != enumconnectionstate1) {
                        PlayerConnect.this.setConnectionState(enumconnectionstate);
                    }

                    ChannelFuture channelfuture1 = PlayerConnect.this.channel.writeAndFlush(inPacket);

                    if (futureListeners != null) {
                        channelfuture1.addListeners(futureListeners);
                    }

                    channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
    }

    public void setNetHandler(INetHandler handler) {
        this.packetListener = handler;
    }

    private void flushOutboundQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            this.readWriteLock.readLock().lock();

            try {
                while (!this.outboundPacketsQueue.isEmpty()) {
                    PlayerConnect.InboundHandlerTuplePacketListener networkmanager$inboundhandlertuplepacketlistener = this.outboundPacketsQueue.poll();
                    this.dispatchPacket(networkmanager$inboundhandlertuplepacketlistener.packet, networkmanager$inboundhandlertuplepacketlistener.futureListeners);
                }
            } finally {
                this.readWriteLock.readLock().unlock();
            }
        }
    }

    public void setConnectionState(PacketIndex newState) {
        this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(newState);
        this.channel.config().setAutoRead(true);
    }

    public void closeChannel(ITextComponent message) {
        if (this.channel.isOpen()) {
            this.channel.close().awaitUninterruptibly();
            this.terminationReason = message;
        }
    }

    public boolean isChannelOpen() {
        return this.channel != null && this.channel.isOpen();
    }

    public void sendPacket(Packet<?> packetIn) {
        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, (GenericFutureListener[]) null);
        } else {
            this.readWriteLock.writeLock().lock();

            try {
                this.outboundPacketsQueue.add(new PlayerConnect.InboundHandlerTuplePacketListener(packetIn, new GenericFutureListener[0]));
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }

    static class InboundHandlerTuplePacketListener {
        private final Packet<?> packet;
        private final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;

        public InboundHandlerTuplePacketListener(Packet<?> inPacket, GenericFutureListener<? extends Future<? super Void>>... inFutureListeners) {
            this.packet = inPacket;
            this.futureListeners = inFutureListeners;
        }
    }
}
