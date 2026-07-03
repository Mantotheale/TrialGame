package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

record GlyphHeader(
        short numberOfCountours,
        short xMin,
        short yMin,
        short xMax,
        short yMax
) {
    public static GlyphHeader fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
        return new GlyphHeader(
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer)
        );
    }

    @Override
    public @NotNull String toString() {
        return "GlyphHeader(" +
                "numberOfCountours: " + numberOfCountours + ", " +
                "xMin: " + xMin + ", " +
                "yMin: " + yMin + ", " +
                "xMax: " + xMax + ", " +
                "yMax: " + yMax +
                ")";
    }
}
