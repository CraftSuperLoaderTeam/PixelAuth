package io.pixel.network.handle.imp;

import io.pixel.PixelCraft;
import io.pixel.network.PlayerConnect;
import io.pixel.network.handle.INetHandlerHandshakeServer;
import io.pixel.network.packet.ClientHandshake;
import io.pixel.network.packet.PacketIndex;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {
    PixelCraft server;
    PlayerConnect network;
    public NetHandlerHandshakeTCP(PixelCraft server, PlayerConnect network){
        this.server = server;
        this.network = network;
    }

    @Override
    public void processHandshake(ClientHandshake packetIn) {
        switch (packetIn.getRequestedState()){
            case STATUS -> {
                this.network.setConnectionState(PacketIndex.STATUS);
                this.network.setNetHandler(new NetHandlerStatusServer(this.server, this.network));
            }
            case LOGIN ->{

            }
            default-> throw new UnsupportedOperationException("Invalid intention " + packetIn.getRequestedState());
        }
    }
}
