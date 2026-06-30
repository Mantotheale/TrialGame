#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec4 aColor;

uniform mat4 viewProjection;
out vec4 vColor;

void main() {
    vColor = aColor;
    gl_Position = viewProjection * vec4(aPos, 0.0, 1.0);
}