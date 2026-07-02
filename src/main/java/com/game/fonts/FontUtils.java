package com.game.fonts;

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
            ByteBuffer u64Buffer = ByteBuffer.allocateDirect(8).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u32Buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.BIG_ENDIAN);
            ByteBuffer u16Buffer = ByteBuffer.allocateDirect(2).order(ByteOrder.BIG_ENDIAN);

            OffsetSubtable offsetSubtable = OffsetSubtable.fromChannel(channel, u16Buffer, u32Buffer, path);
            List<TableHeader> tableHeaders = new ArrayList<>();
            for (int i = 0; i < offsetSubtable.numTables(); i++)
                tableHeaders.add(TableHeader.fromChannel(channel, u32Buffer));

            TableHeader cmapTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("cmap"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a cmap table"));
            channel.position(cmapTableHeader.offset());

            CmapTable cmapTable = CmapTable.fromChannel(channel, u16Buffer, u32Buffer);
            EncodingRecord bestEncoding = cmapTable.encodingRecords().stream().max(EncodingRecord.QUALITY_COMPARATOR).orElseThrow();
            if (!bestEncoding.isUnicodeMapping())
                throw new IllegalStateException("No unicode mapping is supported");
            System.out.println("best encoding: " + bestEncoding);

            int bestEncodingOffset = cmapTableHeader.offset() + bestEncoding.offset();
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

            TableHeader headTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("head"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a head table"));
            channel.position(headTableHeader.offset());

            HeadTable headTable = HeadTable.fromChannel(channel, u16Buffer, u32Buffer, u64Buffer);
            System.out.println(headTable);

            TableHeader hheaTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("hhea"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a hhea table"));
            channel.position(hheaTableHeader.offset());

            HheaTable hheaTable = HheaTable.fromChannel(channel, u16Buffer);
            System.out.println(hheaTable);

            TableHeader maxpTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("maxp"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a maxp table"));
            channel.position(maxpTableHeader.offset());

            short majorVersion = extract16Bit(channel, u16Buffer);
            short minorVersion = extract16Bit(channel, u16Buffer);
            if (majorVersion != 1)
                throw new IllegalStateException("Unsupported maxp table major version. Only version 1 is supported. It was " + Short.toUnsignedInt(majorVersion));
            if (Short.toUnsignedInt(minorVersion) != 0)
                throw new IllegalStateException("Unsupported maxp table minor version. Only version 0 is supported. It was " + Short.toUnsignedInt(minorVersion));

            MaxpTable maxpTable = MaxpTable.fromChannel(channel, u16Buffer);
            System.out.println(maxpTable);

            TableHeader hmtxTableHeader = tableHeaders.stream()
                    .filter(h -> h.tag().equals("hmtx"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("The specified font doesn't contain a hmtx table"));
            channel.position(hmtxTableHeader.offset());

            HmtxTable hmtxTable = HmtxTable.fromChannel(channel, u16Buffer, maxpTable.numGlyphs(), hheaTable.numberOfHMetrics());
            //System.out.println(hmtxTable);
            System.out.println(tableHeaders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static long extract64Bit(ByteChannel channel, ByteBuffer u64Buffer) {
        try {
            channel.read(u64Buffer);
            u64Buffer.flip();
            long value = u64Buffer.getLong();
            u64Buffer.flip();
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
