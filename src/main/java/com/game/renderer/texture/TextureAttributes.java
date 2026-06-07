package com.game.renderer.texture;

public class TextureAttributes {
    final TextureMagnifyingFilter magnifyingFilter;
    final TextureMinifyingFilter minifyingFilter;
    final TextureWrap wrapS;
    final TextureWrap wrapT;
    final boolean mipmap;

    private TextureAttributes(
            TextureMagnifyingFilter magnifyingFilter,
            TextureMinifyingFilter minifyingFilter,
            TextureWrap wrapS,
            TextureWrap wrapT,
            boolean mipmap) {
        this.magnifyingFilter = magnifyingFilter;
        this.minifyingFilter = minifyingFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        this.mipmap = mipmap;
    }

    public static class Builder {
        private TextureMagnifyingFilter magnifyingFilter = TextureMagnifyingFilter.LINEAR;
        private TextureMinifyingFilter minifyingFilter = TextureMinifyingFilter.LINEAR;
        private TextureWrap wrapS = TextureWrap.REPEAT;
        private TextureWrap wrapT = TextureWrap.REPEAT;
        private boolean mipmap = false;

        public Builder magnifyingFilter(TextureMagnifyingFilter filter) {
            this.magnifyingFilter = filter;
            return this;
        }

        public Builder minifyingFilter(TextureMinifyingFilter filter) {
            this.minifyingFilter = filter;
            return this;
        }

        public Builder wrapS(TextureWrap wrap) {
            this.wrapS = wrap;
            return this;
        }

        public Builder wrapT(TextureWrap wrap) {
            this.wrapT = wrap;
            return this;
        }

        public Builder mipmap(boolean mipmap) {
            this.mipmap = mipmap;
            return this;
        }

        public TextureAttributes build() {
            if (!mipmap) {
                if (
                    minifyingFilter == TextureMinifyingFilter.LINEAR_MIPMAP_LINEAR ||
                    minifyingFilter == TextureMinifyingFilter.LINEAR_MIPMAP_NEAREST ||
                    minifyingFilter == TextureMinifyingFilter.NEAREST_MIPMAP_LINEAR ||
                    minifyingFilter == TextureMinifyingFilter.NEAREST_MIPMAP_NEAREST) {
                    throw new RuntimeException("Cannot create TextureAttributes with a " + minifyingFilter.name() + " minifyingFilter without a mipmap");
                }
            }

            return new TextureAttributes(magnifyingFilter, minifyingFilter, wrapS, wrapT, mipmap);
        }
    }
}
