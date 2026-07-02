package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public record SequentialMapGroup(int startCode, int endCode, int startGlyphId) {
    public static SequentialMapGroup fromChannel(ByteChannel channel, ByteBuffer u32Buffer) {
        return new SequentialMapGroup(
                FontUtils.extract32Bit(channel, u32Buffer),
                FontUtils.extract32Bit(channel, u32Buffer),
                FontUtils.extract32Bit(channel, u32Buffer)
        );
    }

    @Override
    public @NotNull String toString() {
        return "SequentialMapGroup(" +
                "startCode: " + Integer.toUnsignedLong(startCode) + ", " +
                "endCode: " + Integer.toUnsignedLong(endCode) + ", " +
                "startGlyphId: " + Integer.toUnsignedLong(startGlyphId) +
                ")";
    }
}
