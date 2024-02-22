package io.pixel.pcall.network.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.pixel.pcall.network.packet.login.*;
import io.pixel.pcall.network.packet.stats.PacketPing;
import io.pixel.pcall.network.packet.stats.PacketPong;
import io.pixel.pcall.network.packet.stats.PacketServerInfo;
import io.pixel.pcall.network.packet.stats.PacketServerQuery;
import org.apache.logging.log4j.LogManager;

import java.util.Map;

public enum PacketIndex {
    HANDSHAKING(-1) {
        {
            this.registerPacket(PacketDirection.SERVERBOUND, ClientHandshake.class);
        }
    },
    STATUS(1) {
        {
            this.registerPacket(PacketDirection.SERVERBOUND, PacketServerQuery.class);
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketServerInfo.class);
            this.registerPacket(PacketDirection.SERVERBOUND, PacketPing.class);
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketPong.class);
        }
    },
    LOGIN(2){
        {
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketDisconnect.class);
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketEncryptionRequest.class);
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketLoginSuccess.class);
            this.registerPacket(PacketDirection.CLIENTBOUND, PacketEnableCompression.class);
            this.registerPacket(PacketDirection.SERVERBOUND, PacketLoginStart.class);
            this.registerPacket(PacketDirection.SERVERBOUND, PacketEncryptionResponse.class);
        }
    }
    ;
    private static final PacketIndex[] STATES_BY_ID = new PacketIndex[4];
    private static final Map<Class<? extends Packet<?>>, PacketIndex> STATES_BY_CLASS = Maps.<Class<? extends Packet<?>>, PacketIndex>newHashMap();
    private final int id;
    private final Map<PacketDirection, BiMap<Integer, Class<? extends Packet<?>>>> directionMaps;

    private PacketIndex(int protocolId) {
        this.directionMaps = Maps.newConcurrentMap();
        this.id = protocolId;
    }

    protected PacketIndex registerPacket(PacketDirection direction, Class<? extends Packet<?>> packetClass) {
        BiMap<Integer, Class<? extends Packet<?>>> bimap = (BiMap) this.directionMaps.get(direction);

        if (bimap == null) {
            bimap = HashBiMap.<Integer, Class<? extends Packet<?>>>create();
            this.directionMaps.put(direction, bimap);
        }

        if (bimap.containsValue(packetClass)) {
            String s = direction + " packet " + packetClass + " is already known to ID " + bimap.inverse().get(packetClass);
            LogManager.getLogger().fatal(s);
            throw new IllegalArgumentException(s);
        } else {
            bimap.put(Integer.valueOf(bimap.size()), packetClass);
            return this;
        }
    }

    public static PacketIndex getById(int stateId) {
        return stateId >= -1 && stateId <= 2 ? STATES_BY_ID[stateId - -1] : null;
    }

    public static PacketIndex getFromPacket(Packet<?> packetIn) {
        return STATES_BY_CLASS.get(packetIn.getClass());
    }

    public Integer getPacketId(PacketDirection direction, Packet<?> packetIn) throws Exception {
        return (Integer) ((BiMap) this.directionMaps.get(direction)).inverse().get(packetIn.getClass());
    }

    public Packet<?> getPacket(PacketDirection direction, int packetId) throws InstantiationException, IllegalAccessException {
        Class<? extends Packet<?>> oclass = (Class) ((BiMap) this.directionMaps.get(direction)).get(Integer.valueOf(packetId));
        return oclass == null ? null : (Packet) oclass.newInstance();
    }

    public int getId() {
        return this.id;
    }

    static {
        for (PacketIndex enumconnectionstate : values()) {
            int i = enumconnectionstate.getId();

            if (i < -1 || i > 2) {
                throw new Error("Invalid protocol ID " + Integer.toString(i));
            }

            STATES_BY_ID[i - -1] = enumconnectionstate;

            for (PacketDirection enumpacketdirection : enumconnectionstate.directionMaps.keySet()) {
                for (Class<? extends Packet<?>> oclass : (enumconnectionstate.directionMaps.get(enumpacketdirection)).values()) {
                    if (STATES_BY_CLASS.containsKey(oclass) && STATES_BY_CLASS.get(oclass) != enumconnectionstate) {
                        throw new Error("Packet " + oclass + " is already assigned to protocol " + STATES_BY_CLASS.get(oclass) + " - can't reassign to " + enumconnectionstate);
                    }

                    try {
                        oclass.newInstance();
                    } catch (Throwable var10) {
                        throw new Error("Packet " + oclass + " fails instantiation checks! " + oclass);
                    }

                    STATES_BY_CLASS.put(oclass, enumconnectionstate);
                }
            }
        }
    }
}
