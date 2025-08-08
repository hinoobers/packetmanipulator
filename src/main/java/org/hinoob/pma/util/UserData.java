package org.hinoob.pma.util;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.hinoob.pma.IncomingPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserData {

    private final UUID uuid;

    public List<IncomingPacket> incomingPackets = new ArrayList<>();
    public List<PacketSendEvent> outgoingPackets = new ArrayList<>();

    public boolean incomingPacketsPaused = false;

    public UserData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }
}
