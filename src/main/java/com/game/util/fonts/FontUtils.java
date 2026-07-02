package com.game.util.fonts;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class FontUtils {
    private FontUtils() { }

    public static void openFont(Path path) {
        try(FileInputStream s = new FileInputStream(path.toFile())) {
            System.out.println("File: " + path);
            FileChannel channel = s.getChannel();
            ByteBuffer u32Buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u16Buffer = ByteBuffer.allocateDirect(2).order(ByteOrder.BIG_ENDIAN);

            OffsetSubtable offsetSubtable = OffsetSubtable.fromChannel(channel, u16Buffer, u32Buffer, path);
            List<TableHeader> tableHeaders = new ArrayList<>();
            for (int i = 0; i < offsetSubtable.numTables(); i++)
                tableHeaders.add(TableHeader.fromChannel(channel, u32Buffer));

            TableHeader cmapHeaderEntry = tableHeaders.stream()
                    .filter(h -> h.tag().equals("cmap"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a cmap table"));
            channel.position(cmapHeaderEntry.offset());

            CmapHeader cmapHeader = CmapHeader.fromChannel(channel, u16Buffer, u32Buffer);
            EncodingRecord bestEncoding = cmapHeader.encodingRecords().stream().max(EncodingRecord.QUALITY_COMPARATOR).orElseThrow();
            if (!bestEncoding.isUnicodeMapping())
                throw new IllegalStateException("No unicode mapping is supported");
            System.out.println("best encoding: " + bestEncoding);

            int bestEncodingOffset = cmapHeaderEntry.offset() + bestEncoding.offset();
            channel.position(bestEncodingOffset);

            int version = extract16Bit(channel, u16Buffer);
            if (version == 4) {
                CmapSubtableFormat4 format = CmapSubtableFormat4.fromChannel(channel, u16Buffer);

                short unicode = '/';
                System.out.println(Short.toUnsignedInt(format.getGlyphId(unicode)));
            } else {
                CmapSubtableFormat12 format = CmapSubtableFormat12.fromChannel(channel, u16Buffer, u32Buffer);
                int unicode = "珠".codePointAt(0);
                System.out.println(Integer.toUnsignedLong(format.getGlyphId(unicode)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringRepresentation(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();

        ByteBuffer readOnly = buffer.duplicate();
        while (readOnly.hasRemaining())
            sb.append(readOnly.get()).append(" ");

        return sb.substring(sb.length() - 1);
    }

    public static int extract32Bit(ByteChannel channel, ByteBuffer u32Buffer) {
        try {
            channel.read(u32Buffer);
            u32Buffer.flip();
            int value = u32Buffer.getInt();
            u32Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static short extract16Bit(ByteChannel channel, ByteBuffer u16Buffer) {
        try {
            channel.read(u16Buffer);
            u16Buffer.flip();
            short value = u16Buffer.getShort();
            u16Buffer.flip();
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        ByteBuffer readOnly = buffer.duplicate();
        while (readOnly.hasRemaining())
            sb.append(HexFormat.of().toHexDigits(readOnly.get()));

        return sb.toString();
    }
}
