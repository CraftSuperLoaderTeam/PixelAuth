package io.pixel.pcall.network;

import com.google.common.collect.Queues;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.pixel.pcall.network.controller.NettyCompressionDecoder;
import io.pixel.pcall.network.controller.NettyCompressionEncoder;
import io.pixel.pcall.network.controller.NettyEncryptingDecoder;
import io.pixel.pcall.network.controller.NettyEncryptingEncoder;
import io.pixel.pcall.network.handle.INetHandler;
import io.pixel.pcall.network.packet.Packet;
import io.pixel.pcall.network.packet.PacketDirection;
import io.pixel.pcall.network.packet.PacketIndex;
import io.pixel.schedule.NetworkTask;
import io.pixel.pcall.util.CryptManager;
import io.pixel.pcall.util.ThreadQuickExitException;
import io.pixel.pcall.util.text.ITextComponent;
import io.pixel.pcall.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.pixel.pcall.network.NetworkServer.PROTOCOL_ATTRIBUTE_KEY;


public class PlayerConnect extends SimpleChannelInboundHandler<Packet<?>> {

    private static final Logger LOGGER = LogManager.getLogger(PlayerConnect.class);
    private final PacketDirection direction;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Queue<PlayerConnect.InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.<PlayerConnect.InboundHandlerTuplePacketListener>newConcurrentLinkedQueue();
    private Channel channel;
    private INetHandler packetListener;
    private ITextComponent terminationReason;
    private boolean disconnected;
    SocketAddress socketAddress;
    private boolean isEncrypted;


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

    public SocketAddress getRemoteAddress() {
        return this.socketAddress;
    }

    public boolean isLocalChannel() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Packet<?> packet) throws Exception {
        if(this.channel.isOpen()){
            try {
                ((Packet<INetHandler>) packet).processPacket(this.packetListener);
            } catch (ThreadQuickExitException ignored) {
            }
        }
    }

    public void enableEncryption(SecretKey key) {
        this.isEncrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
        this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
    }

    public boolean hasNoChannel() {
        return this.channel == null;
    }

    public ITextComponent getExitMessage() {
        return this.terminationReason;
    }

    public void update() {
        this.flushOutboundQueue();

        if (this.packetListener instanceof NetworkTask) {
            ((NetworkTask) this.packetListener).update();
        }

        if (this.channel != null) {
            this.channel.flush();
        }
    }

    public void setCompressionThreshold(int threshold) {
        if (threshold >= 0) {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                ((NettyCompressionDecoder) this.channel.pipeline().get("decompress")).setCompressionThreshold(threshold);
            } else {
                this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(threshold));
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                ((NettyCompressionEncoder) this.channel.pipeline().get("compress")).setCompressionThreshold(threshold);
            } else {
                this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(threshold));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }

            if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {
        if (this.isChannelOpen()) {
            this.flushOutboundQueue();
            this.dispatchPacket(packetIn, (GenericFutureListener[]) ArrayUtils.add(listeners, 0, listener));
        } else {
            this.readWriteLock.writeLock().lock();

            try {
                this.outboundPacketsQueue.add(new PlayerConnect.InboundHandlerTuplePacketListener(packetIn, (GenericFutureListener[]) ArrayUtils.add(listeners, 0, listener)));
            } finally {
                this.readWriteLock.writeLock().unlock();
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
            this.channel.eventLoop().execute(() -> {
                if (enumconnectionstate != enumconnectionstate1) {
                    PlayerConnect.this.setConnectionState(enumconnectionstate);
                }

                ChannelFuture channelfuture1 = PlayerConnect.this.channel.writeAndFlush(inPacket);

                if (futureListeners != null) {
                    channelfuture1.addListeners(futureListeners);
                }

                channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    public INetHandler getNetHandler() {
        return this.packetListener;
    }

    public void setNetHandler(INetHandler handler) {
        this.packetListener = handler;
    }

    public void checkDisconnected() {
        if (this.channel != null && !this.channel.isOpen()) {
            if (this.disconnected) {
                LOGGER.warn("handleDisconnection() called twice");
            } else {
                this.disconnected = true;

                if (this.getExitMessage() != null) {
                    this.getNetHandler().onDisconnect(this.getExitMessage());
                } else if (this.getNetHandler() != null) {
                    this.getNetHandler().onDisconnect(new TextComponentTranslation("multiplayer.disconnect.generic", new Object[0]));
                }
            }
        }
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
