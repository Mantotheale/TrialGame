package com.game.renderer;

import com.game.renderer.texture.Texture;
import com.game.transform.Transform;

record RenderCommand(Transform transform, Texture texture) implements Comparable<RenderCommand> {
    @Override
    public int compareTo(RenderCommand other) {
        float z = this.transform.translation().z();
        float otherZ = other.transform.translation().z();

        return Float.compare(z, otherZ);
    }
}
