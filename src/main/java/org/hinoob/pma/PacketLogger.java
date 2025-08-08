package org.hinoob.pma;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.hinoob.pma.util.UserData;

import java.util.*;

public class PacketLogger {

    private static final Map<UUID, UserData> userData = new HashMap<>();


    public static void logIncoming(UUID uuid, PacketReceiveEvent event) {
        if(uuid == null) return;

        UserData user = userData.getOrDefault(uuid, null);
        if (user == null) {
            user = new UserData(uuid);
            userData.put(uuid, user);
        }
        if(user.incomingPacketsPaused) {
            event.setCancelled(true);
            return; // Skip logging if incoming packets are paused for this user
        }
        IncomingPacket packet = new IncomingPacket(event);

        user.incomingPackets.add(packet);
        PacketManipulator.getInstance().getWebServer().getSessionsFor(uuid).forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getRemote().sendString(packet.toJSON().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void logOutgoing(UUID uuid, PacketSendEvent event) {
        UserData user = userData.getOrDefault(uuid, null);
        if (user == null) {
            user = new UserData(uuid);
            userData.put(uuid, user);
        }
        user.outgoingPackets.add(event);
    }

    public static List<IncomingPacket> getIncomingPackets(UUID uuid) {
        return userData.get(uuid) != null ? userData.get(uuid).incomingPackets : new ArrayList<>();
    }

    public static List<PacketSendEvent> getOutgoingPackets(UUID uuid) {
        return userData.get(uuid) != null ? userData.get(uuid).outgoingPackets : new ArrayList<>();
    }

    public static UserData getUserData(UUID uuid) {
        return userData.getOrDefault(uuid, null);
    }

    public static Collection<UUID> getPlayers() {
        return userData.keySet();
    }

}
