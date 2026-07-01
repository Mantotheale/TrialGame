#version 330

layout (location = 0) in vec2 aCenter;
layout (location = 1) in float aRadius;
layout (location = 2) in vec4 aColor;

uniform mat4 viewProjection;

flat out vec2 vCenter;
flat out float vRadius;
flat out vec4 vColor;

void main() {
    vCenter = aCenter;
    vRadius = aRadius;
    vColor = aColor;
    gl_Position = viewProjection * vec4(aCenter, 0, 1);
}