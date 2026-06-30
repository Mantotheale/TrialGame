#version 330 core

layout (lines) in;
in vec4 vColor[];

layout (triangle_strip, max_vertices = 4) out;
out vec4 gColor;
out float gLineDistance;

uniform vec2 viewportSize;
uniform float lineWidth;

void main() {
    vec4 p0 = gl_in[0].gl_Position;
    vec4 p1 = gl_in[1].gl_Position;

    vec2 ndcP0 = p0.xy / p0.w;
    vec2 ndcP1 = p1.xy / p1.w;
    vec2 zeroToOneP0 = (ndcP0 * 0.5) + 0.5;
    vec2 zeroToOneP1 = (ndcP1 * 0.5) + 0.5;
    vec2 pixelP0 = zeroToOneP0 * viewportSize;
    vec2 pixelP1 = zeroToOneP1 * viewportSize;

    vec2 lineDir = normalize(pixelP1 - pixelP0);
    vec2 lineNormal = vec2(-lineDir.y, lineDir.x);

    float fringe = 1.0;
    float halfWidth = lineWidth * 0.5;
    float totalHalfWidth = halfWidth + fringe;

    vec2 maxDisplacement = lineNormal * totalHalfWidth;

    // Vertex 1: Top-Left (Extended past p0)
    gColor = vColor[0];
    gLineDistance = totalHalfWidth;
    gl_Position = vec4((((pixelP0 + maxDisplacement) / viewportSize - 0.5) * 2.0) * p0.w, p0.z, p0.w);
    EmitVertex();

    // Vertex 2: Bottom-Left (Extended past p0)
    gColor = vColor[0];
    gLineDistance = -totalHalfWidth;
    gl_Position = vec4((((pixelP0 - maxDisplacement) / viewportSize - 0.5) * 2.0) * p0.w, p0.z, p0.w);
    EmitVertex();

    // Vertex 3: Top-Right (Extended past p1)
    gColor = vColor[1];
    gLineDistance = totalHalfWidth;
    gl_Position = vec4((((pixelP1 + maxDisplacement) / viewportSize - 0.5) * 2.0) * p0.w, p0.z, p0.w);
    EmitVertex();

    // Vertex 4: Bottom-Right (Extended past p1)
    gColor = vColor[1];
    gLineDistance = -totalHalfWidth;
    gl_Position = vec4((((pixelP1 - maxDisplacement) / viewportSize - 0.5) * 2.0) * p0.w, p0.z, p0.w);
    EmitVertex();

    EndPrimitive();
}