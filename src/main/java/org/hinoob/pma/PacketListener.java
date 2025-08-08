package org.hinoob.pma;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import io.netty.buffer.ByteBuf;
import org.hinoob.pma.util.TestUtil;

import java.util.Base64;

public class PacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketLogger.logOutgoing(event.getUser().getUUID(), event);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {

            ByteBuf fullBuffer = ((ByteBuf) event.getFullBufferClone()).copy();
            byte[] data = new byte[fullBuffer.readableBytes()];
            fullBuffer.readBytes(data);
            System.out.println("readableBytes: " + data.length);
            System.out.println("A->" + Base64.getEncoder().encodeToString(data));
        }
        PacketLogger.logIncoming(event.getUser().getUUID(), event);
    }
}
