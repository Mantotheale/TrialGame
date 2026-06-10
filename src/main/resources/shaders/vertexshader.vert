#version 330

layout (location = 0) in vec3 aPos;
layout (location = 1) in int aTexIdx;
layout (location = 2) in vec2 aTexCoord;

flat out int texIdx;
out vec2 texCoord;

uniform mat4 viewProjection;

void main() {
    gl_Position = viewProjection * vec4(aPos, 1.0);
    texIdx = aTexIdx;
    texCoord = aTexCoord;
}