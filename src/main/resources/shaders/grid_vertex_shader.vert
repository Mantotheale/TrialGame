#version 330

layout (location = 0) in vec2 aWorldPos;

uniform mat4 viewProjection;

out vec2 vWorldPos;

void main() {
    gl_Position = viewProjection * vec4(aWorldPos, 0, 1.0);
    vWorldPos = aWorldPos;
}