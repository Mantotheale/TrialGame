package com.game.renderer;

import com.game.renderer.indexbuffer.IndexBuffer;
import com.game.renderer.shaderprogram.ShaderProgram;
import com.game.renderer.texture.Texture;
import com.game.renderer.vertexarray.VertexArray;
import com.game.renderer.vertexbuffer.VertexBuffer;

public class GLContext {
    private VertexBuffer boundVertexBuffer;
    private IndexBuffer boundIndexBuffer;
    private VertexArray boundVertexArray;
    private ShaderProgram boundShaderProgram;
    private Texture boundTexture;
}
