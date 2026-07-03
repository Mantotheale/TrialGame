package com.game.fonts;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

sealed interface Glyph {
    Glyph EMPTY = new EmptyGlyph();

    static Glyph fromChannel(ByteChannel channel, ByteBuffer u8Buffer, ByteBuffer u16Buffer, short idx) {
        GlyphHeader glyphHeader = GlyphHeader.fromChannel(channel, u16Buffer, idx);

        if (glyphHeader.numberOfContours() >= 0) {
            return SimpleGlyph.fromChannel(channel, u8Buffer, u16Buffer, glyphHeader);
        } else {
            return CompositeGlyph.fromChannel(channel, u8Buffer, u16Buffer, glyphHeader);
        }
    }

    record EmptyGlyph() implements Glyph {
        @Override
        public @NotNull String toString() {
            return "EmptyGlyph";
        }
    }

    record SimpleGlyph(
            GlyphHeader glyphHeader,
            List<Contour> contours,
            short instructionLength,
            List<Byte> instructions,
            List<SimpleGlyphFlag> flags,
            List<FontPoint> coords
    ) implements Glyph {
        static SimpleGlyph fromChannel(ByteChannel channel, ByteBuffer u8Buffer, ByteBuffer u16Buffer, GlyphHeader glyphHeader) {
            List<Contour> contours = new ArrayList<>();
            if (glyphHeader.numberOfContours() != 0)
                contours.add(Contour.firstContour(FontUtils.extract16Bit(channel, u16Buffer)));
            for (int i = 1; i < glyphHeader.numberOfContours(); i++)
                contours.add(Contour.fromPreviousContour(contours.get(i - 1), FontUtils.extract16Bit(channel, u16Buffer)));

            short instructionLength = FontUtils.extract16Bit(channel, u16Buffer);
            List<Byte> instructions = new ArrayList<>();
            for (int i = 0; i < Short.toUnsignedInt(instructionLength); i++)
                instructions.add(FontUtils.extract8Bit(channel, u8Buffer));

            List<SimpleGlyphFlag> flags = new ArrayList<>();
            if (!contours.isEmpty()) {
                int processedPoints = 0;
                while (processedPoints < Short.toUnsignedInt(contours.getLast().end()) + 1) {
                    SimpleGlyphFlag flag = new SimpleGlyphFlag(FontUtils.extract8Bit(channel, u8Buffer));
                    flags.add(flag);
                    processedPoints++;

                    if (flag.repeat()) {
                        SimpleGlyphFlag nonRepeatFlag = flag.toNonRepeat();

                        byte repetitions = FontUtils.extract8Bit(channel, u8Buffer);
                        for (int i = 0; i < Byte.toUnsignedInt(repetitions); i++) {
                            flags.add(nonRepeatFlag);
                            processedPoints++;
                        }
                    }
                }
            }

            List<Short> x = new ArrayList<>();
            short currentX = 0;
            for (SimpleGlyphFlag f: flags) {
                short delta;

                if (f.xShortVector()) {
                    int sign = f.isSameOrPositiveXShortVector() ? 1 : -1;
                    int value = Byte.toUnsignedInt(FontUtils.extract8Bit(channel, u8Buffer));
                    delta = (short) (sign * value);
                } else
                if (f.isSameOrPositiveXShortVector())
                    delta = 0;
                else
                    delta = FontUtils.extract16Bit(channel, u16Buffer);

                currentX += delta;
                x.add(currentX);
            }

            List<Short> y = new ArrayList<>();
            short currentY = 0;
            for (SimpleGlyphFlag f: flags) {
                short delta;

                if (f.yShortVector()) {
                    int sign = f.isSameOrPositiveYShortVector() ? 1 : -1;
                    int value = Byte.toUnsignedInt(FontUtils.extract8Bit(channel, u8Buffer));
                    delta = (short) (sign * value);
                } else
                if (f.isSameOrPositiveYShortVector())
                    delta = 0;
                else
                    delta = FontUtils.extract16Bit(channel, u16Buffer);

                currentY += delta;
                y.add(currentY);
            }

            List<FontPoint> points = new ArrayList<>();
            for (int i = 0; i < flags.size(); i++)
                points.add(new FontPoint(x.get(i), y.get(i), flags.get(i).onCurve()));

            return new SimpleGlyph(glyphHeader, contours, instructionLength, instructions, flags, points);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("SimpleGlyph(\n")
                    .append("\tglyphHeader: ").append(glyphHeader).append("\n");

            sb.append("\tendPointsOfContours:\n");
            for (Contour c: contours)
                sb.append("\t\t").append(c).append("\n");
            sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append("\n");

            sb.append("\tinstructionLength: ").append(Short.toUnsignedInt(instructionLength)).append("\n");
            sb.append("\tinstructions:\n");
            for (Byte instr: instructions)
                sb.append("\t\t").append(Byte.toUnsignedInt(instr)).append("\n");

            sb.append("\tflags:\n");
            for (SimpleGlyphFlag f: flags)
                sb.append("\t\t").append(f).append("\n");

            sb.append("\tcoords:\n");
            for (FontPoint fc: coords)
                sb.append("\t\t").append(fc).append("\n");

            return sb.append(")").toString();
        }
    }

    record CompositeGlyph(GlyphHeader glyphHeader, List<GlyphComponent> components, List<Byte> instructions) implements Glyph {
        static CompositeGlyph fromChannel(ByteChannel channel, ByteBuffer u8Buffer, ByteBuffer u16Buffer, GlyphHeader glyphHeader) {
            List<GlyphComponent> components = new ArrayList<>();

            CompositeGlyphFlag flag;
            boolean weHaveInstructions;
            do {
                flag = new CompositeGlyphFlag(FontUtils.extract16Bit(channel, u16Buffer));
                weHaveInstructions = flag.weHaveInstructions();
                short glyphIdx = FontUtils.extract16Bit(channel, u16Buffer);

                GlyphComponentArguments args;
                if (flag.arg1and2areWords()) {
                    short arg1 = FontUtils.extract16Bit(channel, u16Buffer);
                    short arg2 = FontUtils.extract16Bit(channel, u16Buffer);
                    if (flag.argsAreXYValues())
                        args = new GlyphComponentArguments.Offset(arg1, arg2);
                    else
                        args = new GlyphComponentArguments.Points(arg1, arg2);
                } else {
                    byte arg1 = FontUtils.extract8Bit(channel, u8Buffer);
                    byte arg2 = FontUtils.extract8Bit(channel, u8Buffer);

                    if (flag.argsAreXYValues())
                        args = new GlyphComponentArguments.Offset(arg1, arg2);
                    else
                        args = new GlyphComponentArguments.Points((short) Byte.toUnsignedInt(arg1), (short) Byte.toUnsignedInt(arg2));
                }

                GlyphTransformation transformation;
                if (flag.weHaveAScale()) {
                    transformation = new GlyphTransformation.UniformScale(
                            FontUtils.extractF2Dot14(channel, u16Buffer),
                            flag.scaledComponentOffset(),
                            flag.roundXYtoGrid()
                    );
                } else if (flag.weHaveAnXYScale()) {
                    float xScale = FontUtils.extractF2Dot14(channel, u16Buffer);
                    float yScale = FontUtils.extractF2Dot14(channel, u16Buffer);
                    transformation = new GlyphTransformation.UnevenScale(
                            xScale,
                            yScale,
                            flag.scaledComponentOffset(),
                            flag.roundXYtoGrid()
                    );
                } else if (flag.weHaveA2by2()) {
                    float s00 = FontUtils.extractF2Dot14(channel, u16Buffer);
                    float s01 = FontUtils.extractF2Dot14(channel, u16Buffer);
                    float s10 = FontUtils.extractF2Dot14(channel, u16Buffer);
                    float s11 = FontUtils.extractF2Dot14(channel, u16Buffer);
                    transformation = new GlyphTransformation.MatrixScale(
                            s00, s01, s10, s11,
                            flag.scaledComponentOffset(),
                            flag.roundXYtoGrid()
                    );
                } else {
                    transformation = flag.roundXYtoGrid()
                            ? GlyphTransformation.ROUNDED_IDENTITY
                            : GlyphTransformation.UNROUNDED_IDENTITY;
                }

                components.add(new GlyphComponent(glyphIdx, args, transformation, flag.useMyMetrics()));
            } while (flag.moreComponents());

            List<Byte> instructions = new ArrayList<>();
            if (weHaveInstructions) {
                short instructionsCount = FontUtils.extract16Bit(channel, u16Buffer);
                for (int i = 0; i < Short.toUnsignedInt(instructionsCount); i++)
                    instructions.add(FontUtils.extract8Bit(channel, u8Buffer));
            }

            return new CompositeGlyph(glyphHeader, components, instructions);
        }

        @Override
        public @NotNull String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("CompositeGlyph(\n")
                    .append("\tglyphHeader: ").append(glyphHeader).append("\n");

            sb.append("\tcomponents:\n");
            for (GlyphComponent c: components)
                sb.append("\t\t").append(c).append(",\n");

            sb.append("\tinstructions:\n");
            for (byte b: instructions)
                sb.append("\t\t").append(b).append(",\n");

            sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1)
                    .append("\n").append(")");

            return sb.toString();
        }
    }
}
