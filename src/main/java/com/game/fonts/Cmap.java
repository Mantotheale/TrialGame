package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

sealed interface Cmap {
    int getGlyphId(int unicode);

    static Cmap fromChannel(short format, ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer) {
        return switch (format) {
            case 4 -> Format4.fromChannel(channel, u16Buffer);
            case 12 -> Format12.fromChannel(channel, u16Buffer, u32Buffer);
            default -> throw new IllegalArgumentException("The format " + format + " is not supported. The only supported formats are 4 and 12");
        };
    }

    record Format4 (
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
    ) implements Cmap {
        static Format4 fromChannel(ByteChannel channel, ByteBuffer u16Buffer) {
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

            return new Format4(
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

        @Override
        public int getGlyphId(int unicode) {
            if (unicode > 0xFFFF)
                throw new IllegalArgumentException("This character map only supports 2-byte unicodes");
            short code = (short) unicode;

            // The segments (unicode intervals) are ordered. We need to find the one the unicode lies into
            for (int i = 0; i < segments.size(); i++) {
                UnicodeSegment s = segments.get(i);
                if (Short.toUnsignedInt(code) <= Short.toUnsignedInt(s.endCode())) {
                    // If both start and end are greater than the unicode, it means we don't have it
                    if (Short.toUnsignedInt(code) < Short.toUnsignedInt(s.startCode()))
                        return 0;

                    // If the idRangeOffset is 0, we get the glyphId just by adding the offset to the unicode, then apply modulo
                    if (s.idRangeOffset() == 0)
                        return (short) ((Short.toUnsignedInt(code) + Short.toUnsignedInt(s.idDelta())) % 65536);

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

                    return Short.toUnsignedInt(glyphIds.get(startCodeGlyphIdx + (Short.toUnsignedInt(code) - Short.toUnsignedInt(s.startCode()))));
                }
            }

            return 0;
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("Format4(\n")
                    .append("\tformat:").append(Short.toUnsignedInt(format)).append(",\n")
                    .append("\tlength:").append(Short.toUnsignedInt(length)).append(",\n")
                    .append("\tlanguage:").append(Short.toUnsignedInt(language)).append(",\n")
                    .append("\tsegmentCount:").append(Short.toUnsignedInt(segmentCount)).append(",\n")
                    .append("\tsearchRange:").append(Short.toUnsignedInt(searchRange)).append(",\n")
                    .append("\tentrySelector:").append(Short.toUnsignedInt(entrySelector)).append(",\n")
                    .append("\trangeShift:").append(Short.toUnsignedInt(rangeShift)).append(",\n")
                    .append("\treservedPad:").append(Short.toUnsignedInt(reservedPad)).append(",\n")
                    .append("\tsegments:\n");
            for (UnicodeSegment s : segments)
                sb.append("\t\t").append(s).append(",\n");
            sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

            for (Short id : glyphIds)
                sb.append("\t\t").append(id).append(",\n");
            sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

            return sb.append(")").toString();
        }
    }

    record Format12(
            short format,
            short reserved,
            int length,
            int language,
            int numGroups,
            List<SequentialMapGroup> groups
    ) implements Cmap {
        public static Format12 fromChannel(ByteChannel channel, ByteBuffer u16Buffer, ByteBuffer u32Buffer) {
            short format = 12;
            short reserved = FontUtils.extract16Bit(channel, u16Buffer);
            int length = FontUtils.extract32Bit(channel, u32Buffer);
            int language = FontUtils.extract32Bit(channel, u32Buffer);
            int numGroups = FontUtils.extract32Bit(channel, u32Buffer);

            List<SequentialMapGroup> groups = new ArrayList<>();
            for (int i = 0; i < numGroups; i++)
                groups.add(SequentialMapGroup.fromChannel(channel, u32Buffer));

            return new Format12(
                    format,
                    reserved,
                    length,
                    language,
                    numGroups,
                    groups
            );
        }

        @Override
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
}
