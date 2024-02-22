package io.pixel.pcall.network.packet.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.pixel.pcall.network.handle.IStatusClient;
import io.pixel.pcall.network.PacketBuffer;
import io.pixel.pcall.network.ServerStatusResponse;
import io.pixel.pcall.network.packet.Packet;
import io.pixel.pcall.util.EnumTypeAdapterFactory;
import io.pixel.pcall.util.JsonUtils;
import io.pixel.pcall.util.text.ITextComponent;
import io.pixel.pcall.util.text.Style;

import java.io.IOException;

public class PacketServerInfo implements Packet<IStatusClient> {
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


    public void processPacket(IStatusClient handler) {
        handler.handleServerInfo(this);
    }
}
