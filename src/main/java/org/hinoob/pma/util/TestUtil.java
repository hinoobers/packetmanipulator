package org.hinoob.pma.util;

import io.netty.buffer.ByteBuf;

public class TestUtil {

    public static boolean canFieldsBeSerialized(Class<?> clazz) {
        // Check if the class has a public no-arg constructor
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return false; // No public no-arg constructor found
        }

        // Check if all fields are serializable
        for (var field : clazz.getDeclaredFields()) {
            if (!java.io.Serializable.class.isAssignableFrom(field.getType())) {
                return false; // Non-serializable field found
            }
        }

        return true; // All checks passed, class can be serialized
    }

    public static int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        do {
            if (position >= 5) {
                throw new RuntimeException("VarInt too big");
            }
            currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;
        } while ((currentByte & 0x80) != 0);

        return value;
    }
}
