package io.pixel.pcall.network;

import com.google.common.collect.Lists;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.pixel.pcall.PixelCraft;
import io.pixel.pcall.network.controller.NettyPacketDecoder;
import io.pixel.pcall.network.controller.NettyPacketEncoder;
import io.pixel.pcall.network.controller.NettyVarint21FrameDecoder;
import io.pixel.pcall.network.controller.NettyVarint21FrameEncoder;
import io.pixel.pcall.network.handle.imp.HandshakeTCP;
import io.pixel.pcall.network.packet.PacketDirection;
import io.pixel.pcall.network.packet.PacketIndex;
import io.pixel.pcall.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class NetworkServer {
    public static final Logger LOGGER = LogManager.getLogger(NetworkServer.class);
    public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
    private final List<PlayerConnect> connects = Collections.<PlayerConnect>synchronizedList(Lists.newArrayList());
    public static final AttributeKey<PacketIndex> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.<PacketIndex>valueOf("protocol");
    public static final Marker NETWORK_PACKETS_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", NETWORK_MARKER);
    private final List<ChannelFuture> endpoints = Collections.<ChannelFuture>synchronizedList(Lists.newArrayList());
    ThreadFactory acceptFactory = new DefaultThreadFactory("accept");
    ThreadFactory connectFactory = new DefaultThreadFactory("connect");
    NioEventLoopGroup acceptGroup = new NioEventLoopGroup(1, acceptFactory);
    NioEventLoopGroup connectGroup = new NioEventLoopGroup(connectFactory);
    PixelCraft server;
    InetAddress address;
    int port;
    int timeout;

    public NetworkServer(PixelCraft server) {
        try {
            this.address = InetAddress.getByName(server.getConfig().getIp());
            this.port = server.getConfig().getPort();
            this.timeout = server.getConfig().getTimeout();
            this.server = server;
        }catch (UnknownHostException e){
            LOGGER.error("** Unknown host name **");
            LOGGER.error("Please check your config file.");
            LOGGER.error("** Neteork server was throw exception **");
            System.exit(-1);
        }
    }

    public PixelCraft getServer() {
        return server;
    }

    public void connect() throws IOException {
        synchronized (this.endpoints) {
            this.endpoints.add((new ServerBootstrap()).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<Channel>() {
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
                    } catch (ChannelException ignored) {
                    }

                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(timeout))
                            .addLast("legacy_query", new LegacyPingHandler(NetworkServer.this))
                            .addLast("splitter", new NettyVarint21FrameDecoder())
                            .addLast("decoder", new NettyPacketDecoder(PacketDirection.SERVERBOUND))
                            .addLast("prepender", new NettyVarint21FrameEncoder())
                            .addLast("encoder", new NettyPacketEncoder(PacketDirection.CLIENTBOUND));
                    PlayerConnect connect = new PlayerConnect(PacketDirection.SERVERBOUND);
                    NetworkServer.this.connects.add(connect);
                    channel.pipeline().addLast("packet_handler", connect);
                    connect.setNetHandler(new HandshakeTCP(server,connect));

                }
            }).group(acceptGroup,connectGroup).localAddress(address, port).bind().syncUninterruptibly());
        }
        LOGGER.info("server network service: {}:{}",address,port);
    }

    public void update(){
        Iterator<PlayerConnect> iterator = this.connects.iterator();
        while (iterator.hasNext()){
            PlayerConnect connect = iterator.next();
            if(connect.hasNoChannel()){
                if(connect.isChannelOpen()){
                    try{
                        connect.update();
                    }catch (Exception e){
                        LOGGER.error("Player connect was throw exception.",e);
                    }
                }else {
                    iterator.remove();
                    connect.checkDisconnected();
                }
            }
        }
    }

    public void close(){
        LOGGER.info("Closing network I/O...");
        Iterator<PlayerConnect> iterator = this.connects.iterator();
        while (iterator.hasNext()){
            PlayerConnect connect = iterator.next();
            if(connect.hasNoChannel()){
                if(connect.isChannelOpen()){
                    try{
                        connect.getNetHandler().onDisconnect(new TextComponentTranslation("multiplayer.disconnect.server_shutdown"));
                    }catch (Exception e){
                        LOGGER.error("Player connect was throw exception.",e);
                    }
                }else {
                    iterator.remove();
                    connect.checkDisconnected();
                }
            }else iterator.remove();
        }
    }
}
