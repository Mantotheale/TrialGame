package com.game.renderer.texture;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class SimpleTexture {
    private final int id;

    public SimpleTexture(TextureAttributes attributes, Path path) {
        this.id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, attributes.wrapS.glValue());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, attributes.wrapT.glValue());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, attributes.minifyingFilter.glValue());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, attributes.magnifyingFilter.glValue());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = stbi_load(path.toString(), width, height, channels, 4);
            if (image == null) { throw new RuntimeException("Couldn't open image on path: " + path); }

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            stbi_image_free(image);
        }

        if (attributes.mipmap)
            glGenerateMipmap(GL_TEXTURE_2D);
    }

    public int id() {
        return id;
    }

    public void delete() {
        glDeleteTextures(id);
    }
}
