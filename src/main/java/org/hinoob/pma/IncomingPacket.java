package org.hinoob.pma;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.checkerframework.checker.units.qual.A;
import org.hinoob.pma.util.TestUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class IncomingPacket {

    private User from;
    private String packetName;
    private PacketTypeCommon type;
    private long timestamp;
    private String uniqueId;
    private byte[] data;

    public IncomingPacket(PacketReceiveEvent event) {
        this.from = event.getUser();
        this.packetName = event.getPacketType().getName();
        this.type = event.getPacketType();
        this.timestamp = event.getTimestamp();
        this.uniqueId = UUID.randomUUID().toString();
        ByteBuf buf = (ByteBuf) event.getFullBufferClone();
        this.data = new byte[buf.readableBytes()];
        buf.readBytes(this.data);
        buf.release(); // Release the buffer to prevent memory leaks
    }

    public IncomingPacket() {

    }

    public static IncomingPacket from(JsonObject object) {
        System.out.println("Trying to deserialize IncomingPacket from JSON: " + object.toString());

        UUID fromUuid = UUID.fromString(object.get("from").getAsString());
        User user = PacketEvents.getAPI().getProtocolManager().getUser(PacketEvents.getAPI().getProtocolManager().getChannel(fromUuid));

        String packetName = object.get("packetName").getAsString();
        PacketTypeCommon type = PacketType.getById(PacketSide.valueOf(object.get("packetTypeSide").getAsString()), ConnectionState.valueOf(object.get("packetTypeState").getAsString()), ClientVersion.V_1_21_5, object.get("packetTypeId").getAsInt());
        long timestamp = object.get("timestamp").getAsLong();
        String uniqueId = object.get("uniqueId").getAsString();
        byte[] data = Base64.getDecoder().decode(object.get("data").getAsString());

        IncomingPacket packet = new IncomingPacket();
        packet.from = user;
        packet.packetName = packetName;
        packet.type = type;
        packet.timestamp = timestamp;
        packet.uniqueId = uniqueId;
        packet.data = data;

        return packet;
    }

    public JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("from", this.from.getUUID().toString());
        json.addProperty("packetName", this.packetName);
        json.addProperty("packetTypeSide", this.type.getSide().name());
        json.addProperty("packetTypeId", this.type.getId(ClientVersion.V_1_21_5));
        json.addProperty("packetTypeState", from.getConnectionState().name());
        json.addProperty("timestamp", this.timestamp);
        json.addProperty("uniqueId", this.uniqueId);
        json.addProperty("data", Base64.getEncoder().encodeToString(this.data));
        return json;
    }


    public void send() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        PacketWrapper<?> wrapper = new PacketWrapper(ClientVersion.UNKNOWN, PacketEvents.getAPI().getServerManager().getVersion(), this.type.getId(ClientVersion.V_1_21_5));
        wrapper.buffer = buf;
        wrapper.writeBytes(this.data);
        from.receivePacket(wrapper);
    }

}
