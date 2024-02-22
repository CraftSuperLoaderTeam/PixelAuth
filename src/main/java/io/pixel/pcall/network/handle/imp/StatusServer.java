package io.pixel.pcall.network.handle.imp;

import io.pixel.pcall.PixelCraft;
import io.pixel.pcall.network.handle.IStatusServer;
import io.pixel.pcall.network.packet.stats.PacketPing;
import io.pixel.pcall.network.packet.stats.PacketPong;
import io.pixel.pcall.network.packet.stats.PacketServerInfo;
import io.pixel.pcall.network.packet.stats.PacketServerQuery;
import io.pixel.pcall.network.PlayerConnect;
import io.pixel.pcall.util.text.ITextComponent;
import io.pixel.pcall.util.text.TextComponentString;

public class StatusServer implements IStatusServer {
    private static final ITextComponent EXIT_MESSAGE = new TextComponentString("Status request has been handled.");
    private final PixelCraft server;
    private final PlayerConnect connect;
    private boolean handled;

    public StatusServer(PixelCraft serverIn, PlayerConnect connect) {
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
