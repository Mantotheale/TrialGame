package com.game.util.image;

import com.game.util.exceptions.ImageLoadException;
import com.game.util.exceptions.PixelOutOfBoundsException;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Path;

public abstract class Image implements AutoCloseable, PixelSource {
    private final int width;
    private final int height;
    protected final ByteBuffer buffer;
    private boolean isDeleted;

    public static Image load(Path path) {
        return new STBIBackedImage(path);
    }

    public static Image blank(int width, int height) {
        return new LWJGLBackedImage(width, height);
    }

    public static Image copy(PixelSource source) {
        return new LWJGLBackedImage(source);
    }

    protected Image(int width, int height, ByteBuffer buffer) {
        this.width = width;
        this.height = height;
        this.buffer = buffer;
        this.isDeleted = false;
    }

    public int width() {
        if (isDeleted) throw new IllegalStateException("Image has been deleted");
        return width;
    }

    public int height() {
        if (isDeleted) throw new IllegalStateException("Image has been deleted");
        return height;
    }

    public ByteBuffer asRawBuffer() {
        if (isDeleted) throw new IllegalStateException("Image has been deleted");
        return buffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    private int bufferIndex(int x, int y) {
        if (this.isDeleted) throw new IllegalStateException("Image has been deleted");

        if (x < 0 || x >= this.width || y < 0 || y >= this.height)
            throw new PixelOutOfBoundsException(x, y, this.width, this.height);

        return (y * this.width + x) * 4;
    }

    public RGBA getPixel(int x, int y) {
        int offset = this.bufferIndex(x , y);

        return new RGBA(
                this.buffer.get(offset),
                this.buffer.get(offset + 1),
                this.buffer.get(offset + 2),
                this.buffer.get(offset + 3)
        );
    }

    public void setPixel(int x, int y, RGBA color) {
        int offset = this.bufferIndex(x, y);

        this.buffer.put(offset, color.bitsR());
        this.buffer.put(offset + 1, color.bitsG());
        this.buffer.put(offset + 2, color.bitsB());
        this.buffer.put(offset + 3, color.bitsA());
    }

    public void copyPixelSourceAtOffset(PixelSource src, int offsetX, int offsetY) {
        if (this.isDeleted) throw new IllegalStateException("Image has been deleted");

        if (offsetX < 0 || offsetX >= this.width || offsetY < 0 || offsetY >= this.height)
            throw new PixelOutOfBoundsException(offsetX, offsetY, this.width, this.height);

        int srcWidth = src.width();
        int srcHeight = src.height();

        if (offsetX + srcWidth > this.width || offsetY + srcHeight > this.height)
            throw new PixelOutOfBoundsException(offsetX + srcWidth - 1, offsetY + srcHeight - 1, this.width, this.height);

        for (int j = 0; j < srcHeight; j++)
            for (int i = 0; i < srcWidth; i++)
                setPixel(offsetX + i, offsetY + j, src.getPixel(i, j));
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public void delete() {
        if (this.isDeleted) throw new IllegalStateException("Image has been deleted");

        free();
        this.isDeleted = true;
    }

    protected abstract void free();

    @Override
    public void close() {
        if (!this.isDeleted) delete();
    }

    private static class STBIBackedImage extends Image {
        public STBIBackedImage(Path path) {
            ByteBuffer buffer;
            int width, height;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer widthBuffer = stack.mallocInt(1);
                IntBuffer heightBuffer = stack.mallocInt(1);
                IntBuffer channelsBuffer = stack.mallocInt(1);

                STBImage.stbi_set_flip_vertically_on_load(true);
                buffer = STBImage.stbi_load(
                        path.toString(),
                        widthBuffer,
                        heightBuffer,
                        channelsBuffer,
                        4
                );
                if (buffer == null) { throw new ImageLoadException(path, STBImage.stbi_failure_reason()); }

                width = widthBuffer.get(0);
                height = heightBuffer.get(0);
            }

            super(width, height, buffer);
        }

        @Override
        protected void free() {
            STBImage.stbi_image_free(this.buffer);
        }
    }

    private static class LWJGLBackedImage extends Image {
        public LWJGLBackedImage(int width, int height) {
            if (width <= 0 || height <= 0)
                throw new IllegalArgumentException("Image size must be positive, got " + width + "x" + height);

            int size = Math.multiplyExact(Math.multiplyExact(width, height), 4);
            super(width, height, MemoryUtil.memCalloc(size));
        }

        public LWJGLBackedImage(PixelSource src) {
            this(src.width(), src.height());

            try {
                copyPixelSourceAtOffset(src, 0, 0);
            } catch (Exception e) {
                free();
                throw e;
            }
        }

        @Override
        protected void free() {
            MemoryUtil.memFree(this.buffer);
        }
    }
}