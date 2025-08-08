package org.hinoob.pma.util;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;
import com.google.gson.JsonObject;

public class PacketDataUtil {

    public static JsonObject convertToReadable(PacketReceiveEvent event) {
        JsonObject object = new JsonObject();
        if(event.getPacketType() == PacketType.Play.Client.CHAT_MESSAGE) {
            WrapperPlayClientChatMessage wrapper = new WrapperPlayClientChatMessage(event);
            object.addProperty("message", wrapper.getMessage());
            return object;
        } else if(event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
            object.addProperty("slot", wrapper.getSlot());
            return object;
        }

        return null;
    }
}
