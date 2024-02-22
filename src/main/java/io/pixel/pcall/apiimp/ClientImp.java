package io.pixel.pcall.apiimp;

import com.mojang.authlib.GameProfile;
import io.pixel.api.CraftClient;

import java.net.SocketAddress;
import java.util.UUID;

public class ClientImp implements CraftClient {
    GameProfile profile;
    SocketAddress address;
    boolean isDisconnect;
    public ClientImp(GameProfile profile, SocketAddress address){
        this.profile = profile;
        this.address = address;
    }
    @Override
    public UUID getUniqueId() {
        return profile.getId();
    }

    @Override
    public String getName() {
        return profile.getName();
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean isLegacy() {
        return profile.isLegacy();
    }

    @Override
    public void disconnect(String message) {
        this.isDisconnect = true;
    }

    @Override
    public boolean isDisconnect() {
        return isDisconnect;
    }
}
