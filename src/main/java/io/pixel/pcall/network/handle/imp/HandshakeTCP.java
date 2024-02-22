package io.pixel.pcall.network.handle.imp;

import io.pixel.pcall.PixelCraft;
import io.pixel.pcall.network.handle.IHandshakeServer;
import io.pixel.pcall.network.packet.ClientHandshake;
import io.pixel.pcall.network.PlayerConnect;
import io.pixel.pcall.network.packet.PacketIndex;
import io.pixel.pcall.util.text.ITextComponent;

public class HandshakeTCP implements IHandshakeServer {
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
