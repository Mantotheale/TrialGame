package com.game.renderer;

import com.game.renderer.texture.Texture;
import com.game.transform.Transform2D;

record RenderCommand(Transform2D transform, Texture texture) implements Comparable<RenderCommand> {
    @Override
    public int compareTo(RenderCommand other) {
        int z = this.transform.zIndex();
        int otherZ = other.transform.zIndex();
        return Integer.compare(z, otherZ);
    }
}
