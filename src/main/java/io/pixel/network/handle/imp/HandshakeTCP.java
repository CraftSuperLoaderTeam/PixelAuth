package io.pixel.network.handle.imp;

import io.pixel.PixelCraft;
import io.pixel.network.PlayerConnect;
import io.pixel.network.handle.INetHandlerHandshakeServer;
import io.pixel.network.packet.ClientHandshake;
import io.pixel.network.packet.PacketIndex;
import io.pixel.util.text.ITextComponent;

public class HandshakeTCP implements INetHandlerHandshakeServer {
    PixelCraft server;
    PlayerConnect network;
    public HandshakeTCP(PixelCraft server, PlayerConnect network){
        this.server = server;
        this.network = network;
    }

    @Override
    public void processHandshake(ClientHandshake packetIn) {
        switch (packetIn.getRequestedState()){
            case STATUS -> {
                this.network.setConnectionState(PacketIndex.STATUS);
                this.network.setNetHandler(new StatusServer(this.server, this.network));
            }
            case LOGIN ->{
                this.network.setConnectionState(PacketIndex.LOGIN);
                this.network.setNetHandler(new LoginServer(this.server, this.network));
            }
            default-> throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
        }
    }

    @Override
    public void onDisconnect(ITextComponent component) {

    }
}
