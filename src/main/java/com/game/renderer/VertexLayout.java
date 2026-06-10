package com.game.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_INT;

class VertexLayout implements Iterable<LayoutElement> {
    private final List<LayoutElement> layoutElements;
    private final int size;

    private VertexLayout(List<LayoutElement> layoutElements, int size) {
        this.layoutElements = layoutElements;
        this.size = size;
    }

    public int size() { return size; }

    @Override
    public Iterator<LayoutElement> iterator() {
        return Collections.unmodifiableList(layoutElements).iterator();
    }

    static class Builder {
        List<TypeAndCount> typesAndCounts = new ArrayList<>();

        public Builder pushFloats(int count) {
            typesAndCounts.add(new TypeAndCount(ElementType.FLOAT, count));
            return this;
        }

        public Builder pushInts(int count) {
            typesAndCounts.add(new TypeAndCount(ElementType.INT, count));
            return this;
        }

        public VertexLayout build() {
            List<LayoutElement> elements = new ArrayList<>();

            int offset = 0;
            for (int i = 0; i < typesAndCounts.size(); i++) {
                TypeAndCount typeAndCount = typesAndCounts.get(i);
                elements.add(new LayoutElement(i, typeAndCount.count, typeAndCount.type.glType, offset));
                offset += typeAndCount.size();
            }

            return new VertexLayout(elements, offset);
        }

        private record TypeAndCount(ElementType type, int count) {
            public int size() { return count * type.typeSize; }
        }

        private enum ElementType {
            FLOAT(4, GL_FLOAT),
            INT(4, GL_INT);

            private final int typeSize;
            private final int glType;

            ElementType(int typeSize, int glType) {
                this.typeSize = typeSize;
                this.glType = glType;
            }
        }
    }
}
