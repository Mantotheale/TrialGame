package com.game.util.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public record TableHeader(String tag, int offset, int length, int checksum) {
    public static TableHeader fromChannel(ByteChannel channel, ByteBuffer u32Buffer) {
        try {
            byte[] tagArray = new byte[4];
            channel.read(u32Buffer);
            u32Buffer.flip();
            u32Buffer.get(tagArray);
            u32Buffer.flip();
            String tag = new String(tagArray).trim();

            int checksum = FontUtils.extract32Bit(channel, u32Buffer);
            int offset = FontUtils.extract32Bit(channel, u32Buffer);
            int length = FontUtils.extract32Bit(channel, u32Buffer);

            return new TableHeader(tag, offset, length, checksum);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String toString() {
        return "TableHeader(" +
                "tag: " + tag + ", " +
                "offset: " + Integer.toUnsignedLong(offset) + ", " +
                "length: " + Integer.toUnsignedLong(length) + ", " +
                "checkSum: " + Integer.toUnsignedLong(checksum) +
                ")";
    }
}
