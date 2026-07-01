#version 330 core

layout(points) in;
flat in vec2 vCenter[];
flat in float vRadius[];
flat in vec4 vColor[];

layout (triangle_strip, max_vertices = 4) out;
out vec2 gUV;
flat out vec4 gColor;

uniform mat4 viewProjection;

void main() {
    vec2 center = vCenter[0];
    float radius = vRadius[0];
    gColor = vColor[0];

    gUV = vec2(-1, -1);
    gl_Position = viewProjection * vec4(center + vec2(-radius, -radius), 0, 1);
    EmitVertex();
    gUV = vec2(1, -1);
    gl_Position = viewProjection * vec4(center + vec2(radius, -radius), 0, 1);
    EmitVertex();
    gUV = vec2(-1, 1);
    gl_Position = viewProjection * vec4(center + vec2(-radius, radius), 0, 1);
    EmitVertex();
    gUV = vec2(1, 1);
    gl_Position = viewProjection * vec4(center + vec2(radius, radius), 0, 1);
    EmitVertex();

    EndPrimitive();
}