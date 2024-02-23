package io.pixel.api.event;

import io.pixel.api.CraftClient;

public class ClientJoinEvent extends Event{
    CraftClient client;
    public ClientJoinEvent(CraftClient client){
        this.client = client;
    }

    public CraftClient getClient() {
        return client;
    }
}
