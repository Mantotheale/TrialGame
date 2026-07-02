package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

public record CmapSubtableFormat4(
        short format,
        short length,
        short language,
        short segmentCount,
        short searchRange,
        short entrySelector,
        short rangeShift,
        short reservedPad,
        List<UnicodeSegment> segments,
        List<Short> glyphIds
) {
    public static CmapSubtableFormat4 fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
        short format = 4;
        short length = FontUtils.extract16Bit(channel, u16Buffer);
        short language = FontUtils.extract16Bit(channel, u16Buffer);
        short segmentCount = (short) (FontUtils.extract16Bit(channel, u16Buffer) / 2);
        short searchRange = FontUtils.extract16Bit(channel, u16Buffer);
        short entrySelector = FontUtils.extract16Bit(channel, u16Buffer);
        short rangeShift = FontUtils.extract16Bit(channel, u16Buffer);

        // Already read bytes, they are used to know when I can stop reading the glyphIds
        short readBytes = 14;

        List<Short> endCodes = new ArrayList<>();
        do {
            endCodes.add(FontUtils.extract16Bit(channel, u16Buffer));
            readBytes += 2;
        } while (endCodes.getLast() != (short) 0xFFFF);

        short reservedPad = FontUtils.extract16Bit(channel, u16Buffer);
        readBytes += 2;

        List<Short> startCodes = new ArrayList<>();
        do {
            startCodes.add(FontUtils.extract16Bit(channel, u16Buffer));
            readBytes += 2;
        } while (startCodes.getLast() != (short) 0xFFFF);

        List<Short> idDeltas = new ArrayList<>();
        for (int i = 0; i < endCodes.size(); i++) {
            idDeltas.add(FontUtils.extract16Bit(channel, u16Buffer));
            readBytes += 2;
        }

        List<Short> idRangeOffsets = new ArrayList<>();
        for (int i = 0; i < endCodes.size(); i++) {
            idRangeOffsets.add(FontUtils.extract16Bit(channel, u16Buffer));
            readBytes += 2;
        }

        List<Short> glyphIds = new ArrayList<>();
        while (readBytes < length) {
            glyphIds.add(FontUtils.extract16Bit(channel, u16Buffer));
            readBytes += 2;
        }

        List<UnicodeSegment> segments = new ArrayList<>();
        for (int i = 0; i < endCodes.size(); i++)
            segments.add(
                    new UnicodeSegment(
                            startCodes.get(i),
                            endCodes.get(i),
                            idDeltas.get(i),
                            idRangeOffsets.get(i)
                    )
            );

        return new CmapSubtableFormat4(
                format,
                length,
                language,
                segmentCount,
                searchRange,
                entrySelector,
                rangeShift,
                reservedPad,
                segments,
                glyphIds
        );
    }

    public short getGlyphId(short unicode) {
        // The segments (unicode intervals) are ordered. We need to find the one the unicode lies into
        for (int i = 0; i < segments.size(); i++) {
            UnicodeSegment s = segments.get(i);
            if (Short.toUnsignedInt(unicode) <= Short.toUnsignedInt(s.endCode())) {
                // If both start and end are greater than the unicode, it means we don't have it
                if (Short.toUnsignedInt(unicode) < Short.toUnsignedInt(s.startCode()))
                    return 0;

                // If the idRangeOffset is 0, we get the glyphId just by adding the offset to the unicode, then apply modulo
                if (s.idRangeOffset() == 0)
                    return (short) ((Short.toUnsignedInt(unicode) + Short.toUnsignedInt(s.idDelta())) % 65536);

                // If the idRangeOffset is not 0, we get the relative location inside this segment. Let's say rel = unicode - s.startCode.
                // IdRangeOffset is the byte distance between the location of the IdRangeOffset 2-byte word and the location of the startCode's corresponding glyphId in the glyphIds array.
                // So then, the distance between the startCode and the glyph corresponding to our desired character is rel + idRangeOffset.
                // The problem is that we have abstracted the data, so we didn't keep track of byte distances. We have to recalculate them.
                // We have 8 16-bit values and 4 16-bit arrays of length segmentCount before the glyphId array.
                int preGlyphArrayBytes = ((8 + 4 * Short.toUnsignedInt(segmentCount)) * Short.BYTES);
                // We now want to find the address of the idRangeOffset word. To do that, we first find the idRangeOffset array start location.
                // Before it, we have 8 16-bit values and 3 16-bit arrays of length segmentCount
                int preIdRangeOffsetBytes = ((8 + 3 * Short.toUnsignedInt(segmentCount)) * Short.BYTES);
                // To find the address inside the array, we luckily have the index i. Since the elements have size 2 bytes, we just multiply i by 2
                int startJumpBytes = preIdRangeOffsetBytes + i * Short.BYTES;
                // Now to find byte address into the glyphId array, we just subtract from the jump amount (idRangeOffset) the distance from the jump location to the start of the glyphId array.
                int distanceFromGlyphArray = preGlyphArrayBytes - startJumpBytes;
                int byteOffsetIntoGlyphArray = s.idRangeOffset() - distanceFromGlyphArray;
                // Since the elements inside the glyphId array have size 2 bytes, we just divide the offset by 2 to get the index
                int startCodeGlyphIdx = byteOffsetIntoGlyphArray / Short.BYTES;

                return glyphIds.get(startCodeGlyphIdx + (Short.toUnsignedInt(unicode) - Short.toUnsignedInt(s.startCode())));
            }
        }

        return 0;
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("CmapSubtableFormat(\n")
                .append("\tformat:").append(Short.toUnsignedInt(format)).append(",\n")
                .append("\tlength:").append(Short.toUnsignedInt(length)).append(",\n")
                .append("\tlanguage:").append(Short.toUnsignedInt(language)).append(",\n")
                .append("\tsegmentCount:").append(Short.toUnsignedInt(segmentCount)).append(",\n")
                .append("\tsearchRange:").append(Short.toUnsignedInt(searchRange)).append(",\n")
                .append("\tentrySelector:").append(Short.toUnsignedInt(entrySelector)).append(",\n")
                .append("\trangeShift:").append(Short.toUnsignedInt(rangeShift)).append(",\n")
                .append("\treservedPad:").append(Short.toUnsignedInt(reservedPad)).append(",\n")
                .append("\tsegments:\n");
        for (UnicodeSegment s: segments)
            sb.append("\t\t").append(s).append(",\n");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

        for (Short id: glyphIds)
            sb.append("\t\t").append(id).append(",\n");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

        return sb.append(")").toString();
    }
}