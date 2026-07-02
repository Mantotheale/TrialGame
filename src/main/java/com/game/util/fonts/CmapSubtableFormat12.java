package com.game.util.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

public record CmapSubtableFormat12(
        short format,
        short reserved,
        int length,
        int language,
        int numGroups,
        List<SequentialMapGroup> groups
) {
    public static CmapSubtableFormat12 fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer) {
        short format = 12;
        short reserved = FontUtils.extract16Bit(channel, u16Buffer);
        int length = FontUtils.extract32Bit(channel, u32Buffer);
        int language = FontUtils.extract32Bit(channel, u32Buffer);
        int numGroups = FontUtils.extract32Bit(channel, u32Buffer);

        List<SequentialMapGroup> groups = new ArrayList<>();
        for (int i = 0; i < numGroups; i++)
            groups.add(SequentialMapGroup.fromChannel(channel, u32Buffer));

        return new CmapSubtableFormat12(
                format,
                reserved,
                length,
                language,
                numGroups,
                groups
        );
    }

    public int getGlyphId(int unicode) {
        // The groups (unicode intervals) are ordered. We just have to find the interval in which our unicode lies
        for (SequentialMapGroup g: groups) {
            if (Integer.toUnsignedLong(unicode) <= Integer.toUnsignedLong(g.endCode())) {
                // If both start and end are greater than the unicode, it means we don't have it
                if (Integer.toUnsignedLong(unicode) < Integer.toUnsignedLong(g.startCode())) {
                    return 0;
                }

                // Otherwise we just add to the startGlyph the offset of our unicode inside the interval
                long offset = Integer.toUnsignedLong(unicode) - Integer.toUnsignedLong(g.startCode());
                long glyphId = Integer.toUnsignedLong(g.startGlyphId()) + offset;
                return (int) glyphId;
            }
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("CmapSubtableFormat(\n")
                .append("\tformat:").append(Short.toUnsignedInt(format)).append(",\n")
                .append("\treserved:").append(Short.toUnsignedInt(reserved)).append(",\n")
                .append("\tlength:").append(Integer.toUnsignedLong(length)).append(",\n")
                .append("\tlanguage:").append(Integer.toUnsignedLong(language)).append(",\n")
                .append("\tnumGroups:").append(Integer.toUnsignedLong(numGroups)).append(",\n")
                .append("\tgroups:\n");
        for (SequentialMapGroup g: groups)
            sb.append("\t\t").append(g).append(",\n");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

        return sb.append(")").toString();
    }
}
