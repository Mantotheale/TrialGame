package com.game.util.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Comparator;

record EncodingRecord(short platformId, short encodingId, int offset) {
    public static EncodingRecord fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer) {
        return new EncodingRecord(
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract16Bit(channel, u16Buffer),
                FontUtils.extract32Bit(channel, u32Buffer)
        );
    }

    boolean isUnicodeMapping() {
        // platformId = 0 means Unicode platform (or apple)
        // encodingId = 3 means Unicode BMP only encoding, while 4 is the full repertoire
        if (platformId == 0 && (encodingId == 3 || encodingId == 4))
            return true;

        // platformId = 3 means Windows platform (it is actually used by everyone else too)
        // encodingId = 1 means Unicode BMP only encoding, while 10 is the full repertoire
        return  platformId == 3 && (encodingId == 1 || encodingId == 10);
    }

    int qualityRank() {
        if (platformId == 0) {
            if (encodingId == 3) return 1;
            if (encodingId == 4) return 2;
        } else if (platformId == 3) {
            if (encodingId == 1) return 1;
            if (encodingId == 10) return 2;
        }

        return 0;
    }

    public static Comparator<EncodingRecord> QUALITY_COMPARATOR = Comparator.comparingInt(EncodingRecord::qualityRank);

    @Override
    public @NotNull String toString() {
        return "EncodingRecord(" +
                "platformId: " + Short.toUnsignedInt(platformId) + ", " +
                "encodingId: " + Short.toUnsignedInt(encodingId) + ", " +
                "offset: " + Integer.toUnsignedLong(offset) +
                ")";
    }
}
