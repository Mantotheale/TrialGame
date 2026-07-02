package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Path;

record OffsetSubtable(int version, short numTables, short searchRange, short entrySelector, short rangeShift) {
    public static OffsetSubtable fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer, Path path) {
        try {
            channel.read(u32Buffer);
            u32Buffer.flip();

            if (!u32Buffer.equals(SUPPORTED_VERSION))
                throw new IllegalArgumentException(
                        "The font " + path + "'s version is not supported."
                                + " The only supported version is " + FontUtils.toHex(SUPPORTED_VERSION) + "."
                                + " It was " + FontUtils.toHex(u32Buffer)
                );
            int version = u32Buffer.getInt();
            u32Buffer.flip();

            short numTables = FontUtils.extract16Bit(channel, u16Buffer);
            short searchRange = FontUtils.extract16Bit(channel, u16Buffer);
            short entrySelector = FontUtils.extract16Bit(channel, u16Buffer);
            short rangeShift = FontUtils.extract16Bit(channel, u16Buffer);

            return new OffsetSubtable(version, numTables, searchRange, entrySelector, rangeShift);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String toString() {
        return "OffsetSubtable(" +
                "version: " + version + ", " +
                "numTables: " + Short.toUnsignedInt(numTables) + ", " +
                "searchRange: " + Short.toUnsignedInt(searchRange) + ", " +
                "entrySelector: " + Short.toUnsignedInt(entrySelector) + ", " +
                "rangeShift: " + Short.toUnsignedInt(rangeShift) +
                ")";
    }

    private final static ByteBuffer SUPPORTED_VERSION = ByteBuffer.wrap(new byte[] { 0, 1, 0, 0 });
}