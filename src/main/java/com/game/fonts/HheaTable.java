package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

record HheaTable(
        short majorVersion,
        short minorVersion,
        short ascender,
        short descender,
        short lineGap,
        short advanceWidthMax,
        short minLeftSideBearing,
        short minRightSideBearing,
        short xMaxExtent,
        short caretSlopeRise,
        short caretSlopeRun,
        short caretOffset,
        short metricDataFormat,
        short numberOfHMetrics
) {
    public static HheaTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
        short majorVersion = FontUtils.extract16Bit(channel, u16Buffer);
        short minorVersion = FontUtils.extract16Bit(channel, u16Buffer);
        short ascender = FontUtils.extract16Bit(channel, u16Buffer);
        short descender = FontUtils.extract16Bit(channel, u16Buffer);
        short lineGap = FontUtils.extract16Bit(channel, u16Buffer);
        short advanceWidthMax = FontUtils.extract16Bit(channel, u16Buffer);
        short minLeftSideBearing = FontUtils.extract16Bit(channel, u16Buffer);
        short minRightSideBearing = FontUtils.extract16Bit(channel, u16Buffer);
        short xMaxExtent = FontUtils.extract16Bit(channel, u16Buffer);
        short caretSlopeRise = FontUtils.extract16Bit(channel, u16Buffer);
        short caretSlopeRun = FontUtils.extract16Bit(channel, u16Buffer);
        short caretOffset = FontUtils.extract16Bit(channel, u16Buffer);
        FontUtils.extract16Bit(channel, u16Buffer);
        FontUtils.extract16Bit(channel, u16Buffer);
        FontUtils.extract16Bit(channel, u16Buffer);
        FontUtils.extract16Bit(channel, u16Buffer);
        short metricDataFormat = FontUtils.extract16Bit(channel, u16Buffer);
        short numberOfHMetrics =FontUtils.extract16Bit(channel, u16Buffer);

        return new HheaTable(
                majorVersion, minorVersion, ascender, descender, lineGap, advanceWidthMax,
                minLeftSideBearing, minRightSideBearing, xMaxExtent, caretSlopeRise, caretSlopeRun,
                caretOffset, metricDataFormat, numberOfHMetrics
        );
    }

    @Override
    public @NotNull String toString() {
        return "HheaTable(" +
                "majorVersion: " + Short.toUnsignedInt(majorVersion) + ", " +
                "minorVersion: " + Short.toUnsignedInt(minorVersion) + ", " +
                "ascender: " + ascender + ", " +
                "descender: " + descender + ", " +
                "lineGap: " + lineGap + ", " +
                "advanceWidthMax: " + Short.toUnsignedInt(advanceWidthMax) + ", " +
                "minLeftSideBearing: " + minLeftSideBearing + ", " +
                "minRightSideBearing: " + minRightSideBearing + ", " +
                "xMaxExtent: " + xMaxExtent + ", " +
                "caretSlopeRise: " + caretSlopeRise + ", " +
                "caretSlopeRun: " + caretSlopeRun + ", " +
                "caretOffset: " + caretOffset + ", " +
                "metricDataFormat: " + metricDataFormat + ", " +
                "numberOfHMetrics: " + Short.toUnsignedInt(numberOfHMetrics) +
                ")";
    }
}
