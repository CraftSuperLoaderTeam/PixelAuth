package io.pixel.api.event;

import io.pixel.api.CraftClient;

public class ClientLoginEvent extends Event{
    CraftClient client;

    public ClientLoginEvent(CraftClient client){
        this.client = client;
    }

    public CraftClient getClient() {
        return client;
    }
}
