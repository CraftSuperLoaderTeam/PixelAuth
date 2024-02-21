package io.pixel.network.packet.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.pixel.network.PacketBuffer;
import io.pixel.network.ServerStatusResponse;
import io.pixel.network.handle.INetHandlerStatusClient;
import io.pixel.network.packet.Packet;
import io.pixel.util.EnumTypeAdapterFactory;
import io.pixel.util.JsonUtils;
import io.pixel.util.text.ITextComponent;
import io.pixel.util.text.Style;

import java.io.IOException;

public class PacketServerInfo implements Packet<INetHandlerStatusClient> {
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ServerStatusResponse.Version.class,
            new ServerStatusResponse.Version.Serializer())
            .registerTypeAdapter(ServerStatusResponse.Players.class, new ServerStatusResponse.Players.Serializer())
            .registerTypeAdapter(ServerStatusResponse.class, new ServerStatusResponse.Serializer())
            .registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();
    private ServerStatusResponse response;

    public PacketServerInfo() {
    }

    public PacketServerInfo(ServerStatusResponse responseIn) {
        this.response = responseIn;
    }


    public void readPacketData(PacketBuffer buf) throws IOException {
        this.response = (ServerStatusResponse) JsonUtils.gsonDeserialize(GSON, buf.readStringFromBuffer(32767), ServerStatusResponse.class);
    }


    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeString(GSON.toJson(this.response));
    }


    public void processPacket(INetHandlerStatusClient handler) {
        handler.handleServerInfo(this);
    }
}
