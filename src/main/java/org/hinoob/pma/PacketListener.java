package org.hinoob.pma;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import org.hinoob.pma.util.TestUtil;

public class PacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketLogger.logOutgoing(event.getUser().getUUID(), event);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        PacketLogger.logIncoming(event.getUser().getUUID(), event);
    }
}
