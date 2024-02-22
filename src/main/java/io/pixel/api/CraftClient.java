package io.pixel.api;

import java.net.SocketAddress;
import java.util.UUID;

public interface CraftClient {
    UUID getUniqueId();
    String getName();
    SocketAddress getAddress();
    boolean isLegacy();
    void disconnect(String message);
    boolean isDisconnect();
}
