package com.game.renderer;

import com.game.camera.Camera;
import com.game.renderer.texture.Texture;
import com.game.transform.Transform;

public class Renderer {
    public Renderer() { }

    public void beginScene(Camera camera) { }

    public void submit(Transform transform, Texture texture) { }

    public void endScene() { }
}
