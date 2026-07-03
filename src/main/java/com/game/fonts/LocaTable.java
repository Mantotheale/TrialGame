package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

record LocaTable(List<OffsetAndLength> offsetsAndLenghts) {
    public static LocaTable fromChannelIndexShort(ByteChannel channel, ByteBuffer u16Buffer, short numGlyphs) {
        return fromChannel(channel, u16Buffer, null, numGlyphs, false);
    }

    public static LocaTable fromChannelIndexInt(ByteChannel channel, ByteBuffer u32Buffer, short numGlyphs) {
        return fromChannel(channel, null, u32Buffer, numGlyphs, true);
    }

    private static LocaTable fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer, short numGlyphs, boolean areInts) {
        List<Integer> offsets = new ArrayList<>();
        List<Integer> lengths = new ArrayList<>();

        if (numGlyphs > 0) {
            if (areInts)
                offsets.add(FontUtils.extract32Bit(channel, u32Buffer));
            else
                offsets.add(Short.toUnsignedInt(FontUtils.extract16Bit(channel, u16Buffer)) * 2);

            // The last entry is a dummy, just there to get the correct length of the last glyph data
            for (int i = 1; i < numGlyphs + 1; i++) {
                if (areInts)
                    offsets.add(FontUtils.extract32Bit(channel, u32Buffer));
                else
                    offsets.add(Short.toUnsignedInt(FontUtils.extract16Bit(channel, u16Buffer)) * 2);

                lengths.add((int) (Integer.toUnsignedLong(offsets.getLast()) - Integer.toUnsignedLong(offsets.get(offsets.size() - 2))));
            }
            lengths.add((int) (Integer.toUnsignedLong(offsets.getLast()) - Integer.toUnsignedLong(offsets.get(offsets.size() - 2))));
        }

        List<OffsetAndLength> offsetAndLengths = new ArrayList<>();
        for (int i = 0; i < offsets.size(); i++) {
            offsetAndLengths.add(new OffsetAndLength(offsets.get(i), lengths.get(i)));
        }

        if (!offsetAndLengths.isEmpty())
            offsetAndLengths.removeLast();

        return new LocaTable(offsetAndLengths);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("LocaTable(\n");

        for (OffsetAndLength ol: offsetsAndLenghts)
            sb.append("\t").append(ol).append(",\n");

        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1)
                .append("\n").append(")");

        return sb.toString();
    }

    record OffsetAndLength(int offset, int length) {
        @Override
        public @NotNull String toString() {
            return "OffsetAndLength(" +
                    "offset: " + Integer.toUnsignedLong(offset) + ", " +
                    "length: " + Integer.toUnsignedLong(length) +
                    ")";
        }
    }
}
