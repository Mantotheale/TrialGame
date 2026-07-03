package com.game.fonts;

import com.game.math.Rectangle;
import com.game.math.Vec2f;

import java.util.List;

public class FontData {
    private final Cmap characterMapping;
    private final HmtxTable horizontalMetrics;
    private final GlyfTable glyphTable;

    FontData(Cmap characterMapping, HmtxTable horizontalMetrics, GlyfTable glyphTable) {
        this.characterMapping = characterMapping;
        this.horizontalMetrics = horizontalMetrics;
        this.glyphTable = glyphTable;
    }

    public int getGlyphId(int unicode) {
        return characterMapping.getGlyphId(unicode);
    }

    public GlyphData glyphData(int glyphId) {
        Glyph glyph = glyphTable.glyphs().get(glyphId);

        if (glyph instanceof Glyph.SimpleGlyph(GlyphHeader glyphHeader, List<Contour> contours, _, _, _, List<FontPoint> points)) {
            Vec2f bboxCenter = new Vec2f(
                    glyphHeader.xMin() + (glyphHeader.xMax() - glyphHeader.xMin()) / 2f,
                    glyphHeader.yMin() + (glyphHeader.yMax() - glyphHeader.yMin()) / 2f
            );
            Vec2f bboxDimensions = new Vec2f(
                    glyphHeader.xMax() - glyphHeader.xMin(),
                    glyphHeader.yMax() - glyphHeader.yMin()
            );
            Rectangle boundingBox = new Rectangle(bboxCenter, bboxDimensions);

            return new GlyphData(glyphHeader.idx(), boundingBox, points, contours);
        } else {
            throw new UnsupportedOperationException("Composite glyphs not yet supported");
        }
    }


}
