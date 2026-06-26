package com.game.renderer;

import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;
import com.game.math.Vec2f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

class Batch {
    private final QuadBuffer quadBuffer;
    private final ByteBuffer intermediateBuffer;
    private final int textureUnits;
    private final Map<Integer, Integer> textureBindings;
    private int pushedCommands;

    public Batch(QuadBuffer quadBuffer, ByteBuffer intermediateBuffer, int textureUnits) {
        this.quadBuffer = quadBuffer;
        this.intermediateBuffer = intermediateBuffer;
        this.textureUnits = textureUnits;
        this.textureBindings = new HashMap<>();
        this.pushedCommands = 0;
    }

    public boolean canAddCommand(RenderCommand command) {
        return textureBindings.size() != textureUnits ||
                textureBindings.containsKey(command.texture().texId());
    }

    public void addCommand(RenderCommand command) {
        if (!canAddCommand(command)) throw new IllegalStateException("Can't add more textures to the batch");

        Texture texture = command.texture();
        if (!textureBindings.containsKey(texture.texId())) {
            glActiveTexture(GL_TEXTURE0 + textureBindings.size());
            glBindTexture(GL_TEXTURE_2D, texture.texId());
            textureBindings.put(texture.texId(), textureBindings.size());
        }

        Transform2D transform = command.transform();
        insertVertex(transform, texture, BOTTOM_LEFT, texture.bottomLeft());
        insertVertex(transform, texture, BOTTOM_RIGHT, texture.bottomRight());
        insertVertex(transform, texture, TOP_RIGHT, texture.topRight());
        insertVertex(transform, texture, TOP_LEFT, texture.topLeft());

        pushedCommands++;
    }

    public void flush() {
        quadBuffer.setData(intermediateBuffer.flip());
        glBindVertexArray(quadBuffer.id());
        glDrawElements(GL_TRIANGLES, QuadBuffer.INDICES_PER_QUAD * pushedCommands, GL_UNSIGNED_INT, 0);
        intermediateBuffer.clear();
        textureBindings.clear();
        pushedCommands = 0;
    }

    private void insertVertex(Transform2D transform, Texture texture, Vec2f posCorner, Vec2f texCorner) {
        Vec2f tc = transform.transform(posCorner);
        intermediateBuffer.putFloat(tc.x()).putFloat(tc.y());
        intermediateBuffer.putInt(textureBindings.get(texture.texId()));
        intermediateBuffer.putFloat(texCorner.x()).putFloat(texCorner.y());
    }

    public void delete() {
        quadBuffer.delete();
        MemoryUtil.memFree(intermediateBuffer);
    }

    private final static float MICRO_OVERLAP = 0.005f; // TO AVOID QUAD BLEEDING
    private final static Vec2f BOTTOM_LEFT = new Vec2f(-0.5f - MICRO_OVERLAP, -0.5f - MICRO_OVERLAP);
    private final static Vec2f BOTTOM_RIGHT = new Vec2f(0.5f + MICRO_OVERLAP, -0.5f - MICRO_OVERLAP);
    private final static Vec2f TOP_RIGHT = new Vec2f(0.5f + MICRO_OVERLAP, 0.5f + MICRO_OVERLAP);
    private final static Vec2f TOP_LEFT = new Vec2f(-0.5f - MICRO_OVERLAP, 0.5f + MICRO_OVERLAP);
}
