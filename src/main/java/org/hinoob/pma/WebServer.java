package org.hinoob.pma;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.hinoob.pma.util.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {

    private Javalin javalin;
    private PacketManipulator plugin;

    private final Map<UUID, Set<Session>> listeners = new ConcurrentHashMap<>();

    public WebServer(PacketManipulator manipulator) {
        this.plugin = manipulator;
    }

    public void start(int port) {
        this.javalin = Javalin.create(config -> {
            config.staticFiles.add("/web");
        }).start(port);
        javalin.get("/packets/{uuid}", ctx -> {
            UUID uuid = UUID.fromString(ctx.pathParam("uuid"));
            JsonObject response = new JsonObject();
            JsonArray packets = new JsonArray();
            PacketLogger.getIncomingPackets(uuid).forEach(packet -> {
                JsonObject packetJson = packet.toJSON();
                packets.add(packetJson);
            });
            PacketLogger.getOutgoingPackets(uuid).forEach(packet -> {
                JsonObject packetJson = packet.toJSON();
                packets.add(packetJson);
            });
            response.addProperty("size", packets.size());
            response.add("packets", packets);
            ctx.contentType("application/json");
            ctx.result(response.toString());
        });
        javalin.get("/players", ctx -> {
            JsonArray playersArray = new JsonArray();
            for(UUID uuid : PacketLogger.getPlayers()) {
                if(uuid == null) continue; // TODO
                JsonObject playerJson = new JsonObject();
                playerJson.addProperty("uuid", uuid.toString());
                playerJson.addProperty("name", Bukkit.getOfflinePlayer(uuid).getName());
                playersArray.add(playerJson);
            }
            ctx.contentType("application/json");
            ctx.result(playersArray.toString());
        });
        javalin.ws("/ws/packets/{uuid}", ctx -> {
            ctx.onConnect(wsConnectContext -> listeners.computeIfAbsent(UUID.fromString(wsConnectContext.pathParam("uuid")), k -> ConcurrentHashMap.newKeySet())
                    .add(wsConnectContext.session));
            ctx.onClose(wsCloseContext -> {
                UUID uuid = UUID.fromString(wsCloseContext.pathParam("uuid"));
                Set<Session> sessions = listeners.get(uuid);
                if (sessions != null) {
                    sessions.remove(wsCloseContext.session);
                    if (sessions.isEmpty()) {
                        listeners.remove(uuid);
                    }
                }
            });
        });
        javalin.post("/pausePackets", ctx -> {
            String json = ctx.body();
            JsonObject object = new Gson().fromJson(json, JsonObject.class);

            UUID uuid = UUID.fromString(object.get("uuid").getAsString());
            boolean paused_client_server = object.get("paused_client_server").getAsBoolean();
            boolean paused_server_client = object.get("paused_server_client").getAsBoolean();

            UserData userData = PacketLogger.getUserData(uuid);
            userData.incomingPacketsPaused = paused_client_server;
            userData.outgoingPacketsPaused = paused_server_client;
        });
        javalin.post("/receivePacket", ctx -> {
            String json = ctx.body();
            JsonObject object = new Gson().fromJson(json, JsonObject.class);
            if(object.get("packetTypeSide").getAsString().equals("SERVER")) {
                OutgoingPacket packet = OutgoingPacket.from(object);
                packet.send();
            } else {
                IncomingPacket packet = IncomingPacket.from(object);
                packet.send();
            }
            ctx.result("Success!");
        });
        plugin.getLogger().info("Web server started on port " + port);
    }

    public Set<Session> getSessionsFor(UUID uuid) {
        return listeners.getOrDefault(uuid, ConcurrentHashMap.newKeySet());
    }

    public Javalin getJavalin() {
        return javalin;
    }
}
