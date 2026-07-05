package com.game.fonts;

import com.game.math.Rectangle;
import com.game.math.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FontData {
    private final HeadTable headTable;
    private final Cmap characterMapping;
    private final HmtxTable horizontalMetrics;
    private final GlyfTable glyphTable;

    FontData(HeadTable headTable, Cmap characterMapping, HmtxTable horizontalMetrics, GlyfTable glyphTable) {
        this.headTable = headTable;
        this.characterMapping = characterMapping;
        this.horizontalMetrics = horizontalMetrics;
        this.glyphTable = glyphTable;
    }

    public int fontSize() {
        return Short.toUnsignedInt(headTable.unitsPerEm());
    }

    public int getGlyphId(int unicode) {
        return characterMapping.getGlyphId(unicode);
    }

    public GlyphData glyphData(int glyphId) {
        Glyph glyph = glyphTable.glyphs().get(glyphId);
        LongHorMetric metrics = horizontalMetrics.horizontalMetrics().get(glyphId);

        return switch (glyph) {
            case Glyph.EmptyGlyph _ ->
                    new GlyphData(
                            glyphId,
                            new Rectangle(Vec2f.ZERO, 1, 1),
                            List.of(),
                            List.of(),
                            Short.toUnsignedInt(metrics.advanceWidth()),
                            metrics.leftSideBearing()
                    );
            case Glyph.SimpleGlyph(GlyphHeader glyphHeader, List<Contour> contours, _, _, _, List<FontPoint> points) ->
                    new GlyphData(
                            glyphHeader.idx(),
                            glyphHeader.boundingBox(),
                            points,
                            contours,
                            Short.toUnsignedInt(metrics.advanceWidth()),
                            metrics.leftSideBearing()
                    );
            case Glyph.CompositeGlyph(GlyphHeader glyphHeader, List<GlyphComponent> components, _) -> {
                List<FontPoint> points = new ArrayList<>();
                List<Contour> contours =  new ArrayList<>();
                for (GlyphComponent component: components) {
                    switch (component.arguments()) {
                        case GlyphComponentArguments.Offset(short xOffset, short yOffset) -> {
                            GlyphData glyphData = glyphData(component.glyphIdx());
                            GlyphTransformation transformation = component.transformation();

                            Stream<FontPoint> stream = glyphData.points().stream()
                                    .map(p -> p.applyTransformation(transformation));

                            Vec2f offset = transformation.applyToOffset() ?
                                    transformation.transformPoint(new Vec2f(xOffset, yOffset)) :
                                    new Vec2f(xOffset, yOffset);

                            if (transformation.roundToGrid())
                                offset = offset.roundComponents();

                            short previousPoints = (short) points.size();
                            Vec2f finalOffset = offset;
                            points.addAll(stream.map(p -> p.applyOffset(finalOffset)).toList());
                            contours.addAll(glyphData.contours().stream().map(c -> c.shift(previousPoints)).toList());

                            if (component.useComponentMetrics()) {
                                metrics = horizontalMetrics.horizontalMetrics().get(component.glyphIdx());
                            }
                        }
                        case GlyphComponentArguments.Points(short parentPointId, short childPointId) -> {
                            GlyphData glyphData = glyphData(component.glyphIdx());
                            GlyphTransformation transformation = component.transformation();

                            List<FontPoint> transformedChildPoints = glyphData.points().stream()
                                    .map(p -> p.applyTransformation(transformation))
                                    .toList();

                            FontPoint parentPoint = points.get(parentPointId);
                            FontPoint childPoint = transformedChildPoints.get(childPointId);

                            Vec2f offset = new Vec2f(
                                    parentPoint.x() - childPoint.x(),
                                    parentPoint.y() - childPoint.y()
                            );

                            short previousPoints = (short) points.size();
                            points.addAll(transformedChildPoints.stream().map(p -> p.applyOffset(offset)).toList());
                            contours.addAll(glyphData.contours().stream().map(c -> c.shift(previousPoints)).toList());

                            if (component.useComponentMetrics()) {
                                metrics = horizontalMetrics.horizontalMetrics().get(component.glyphIdx());
                            }
                        }
                    }
                }

                yield new GlyphData(
                        glyphHeader.idx(),
                        glyphHeader.boundingBox(),
                        points,
                        contours,
                        Short.toUnsignedInt(metrics.advanceWidth()),
                        metrics.leftSideBearing()
                );
            }
        };
    }
}
