package io.pixel.network.handle.imp;

import io.pixel.PixelCraft;
import io.pixel.network.PlayerConnect;
import io.pixel.network.handle.INetHandlerStatusServer;
import io.pixel.network.packet.stats.PacketPing;
import io.pixel.network.packet.stats.PacketPong;
import io.pixel.network.packet.stats.PacketServerInfo;
import io.pixel.network.packet.stats.PacketServerQuery;
import io.pixel.util.text.ITextComponent;
import io.pixel.util.text.TextComponentString;

public class NetHandlerStatusServer implements INetHandlerStatusServer {
    private static final ITextComponent EXIT_MESSAGE = new TextComponentString("Status request has been handled.");
    private final PixelCraft server;
    private final PlayerConnect connect;
    private boolean handled;

    public NetHandlerStatusServer(PixelCraft serverIn, PlayerConnect connect) {
        this.server = serverIn;
        this.connect = connect;
    }

    public void onDisconnect(ITextComponent reason) {
    }

    public void processServerQuery(PacketServerQuery packetIn) {
        if (this.handled) {
            this.connect.closeChannel(EXIT_MESSAGE);
        } else {
            this.handled = true;
            this.connect.sendPacket(new PacketServerInfo(this.server.getServerStatusResponse()));
        }
    }

    public void processPing(PacketPing packetIn) {
        this.connect.sendPacket(new PacketPong(packetIn.getClientTime()));
        this.connect.closeChannel(EXIT_MESSAGE);
    }
}
