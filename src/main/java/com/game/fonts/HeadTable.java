package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.time.Instant;

record HeadTable(
        short majorVersion,
        short minorVersion,
        int fontRevision,
        int checksumAdjustment,
        int magicNumber,
        short flags,
        short unitsPerEm,
        Instant created,
        Instant modified,
        short xMin,
        short yMin,
        short xMax,
        short yMax,
        short macStyle,
        short lowestRecPPEM,
        short fontDirectionHint,
        short indexToLocFormat,
        short glyphDataFormat
) {
    public static HeadTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer, ByteBuffer u64Buffer) {
        long macToUnixEpochOffset = 2082844800L;

        return new HeadTable(
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract32Bit(channel, u32Buffer),
                FontUtils.extract32Bit(channel, u32Buffer),
                FontUtils.extract32Bit(channel, u32Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                Instant.ofEpochSecond(FontUtils.extract64Bit(channel, u64Buffer) - macToUnixEpochOffset),
                Instant.ofEpochSecond(FontUtils.extract64Bit(channel, u64Buffer) - macToUnixEpochOffset),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer)
        );
    }

    @Override
    public @NotNull String toString() {
        return "HeadTable(" +
                "majorVersion: " + Short.toUnsignedInt(majorVersion) + ", " +
                "minorVersion: " + Short.toUnsignedInt(minorVersion) + ", " +
                "fontRevision: " + Integer.toUnsignedLong(fontRevision) + ", " +
                "checksumAdjustment: " + Integer.toUnsignedLong(checksumAdjustment) + ", " +
                "magicNumber: " + Integer.toUnsignedLong(magicNumber) + ", " +
                "flags: " + Short.toUnsignedInt(flags) + ", " +
                "unitsPerEm: " + Short.toUnsignedInt(unitsPerEm) + ", " +
                "created: " + created + ", " +
                "modified: " + modified + ", " +
                "xMin: " + xMin + ", " +
                "yMin: " + yMin + ", " +
                "xMax: " + xMax + ", " +
                "yMax: " + yMax + ", " +
                "macStyle: " + Short.toUnsignedInt(macStyle) + ", " +
                "lowestRecPPEM: " + Short.toUnsignedInt(lowestRecPPEM) + ", " +
                "fontDirectionHint: " + fontDirectionHint + ", " +
                "indexToLocFormat: " + indexToLocFormat + ", " +
                "glyphDataFormat: " + glyphDataFormat +
                ")";
    }
}
