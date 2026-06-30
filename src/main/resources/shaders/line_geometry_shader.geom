#version 330 core

layout (lines) in;
in vec4 vColor[];
in float vThickness[];

layout (triangle_strip, max_vertices = 4) out;

out vec4 gColor;
noperspective out float gLineDistance;
noperspective out float gThickness;

uniform vec2 viewportSize;

const float FRINGE = 1.5;

vec4 screenSpaceToClipSpace(vec2 targetPixel, vec2 viewportSize, float z, float w) {
    return vec4(((targetPixel / viewportSize - 0.5) * 2.0) * w, z, w);
}

void main() {
    vec4 p0 = gl_in[0].gl_Position;
    vec4 p1 = gl_in[1].gl_Position;

    vec2 pixelP0 = ((p0.xy / p0.w) * 0.5 + 0.5) * viewportSize;
    vec2 pixelP1 = ((p1.xy / p1.w) * 0.5 + 0.5) * viewportSize;

    vec2 lineDir    = normalize(pixelP1 - pixelP0);
    vec2 lineNormal = vec2(-lineDir.y, lineDir.x);

    float totalHalfWidth0 = (vThickness[0] * 0.5) + FRINGE;
    float totalHalfWidth1 = (vThickness[1] * 0.5) + FRINGE;

    vec2 sideDisplacement0 = lineNormal * totalHalfWidth0;
    vec2 sideDisplacement1 = lineNormal * totalHalfWidth1;

    gColor        = vColor[0];
    gThickness    = vThickness[0];
    gLineDistance = totalHalfWidth0;
    gl_Position = screenSpaceToClipSpace(pixelP0 + sideDisplacement0, viewportSize, p0.z, p0.w);
    EmitVertex();

    gColor        = vColor[0];
    gThickness    = vThickness[0];
    gLineDistance = -totalHalfWidth0;
    gl_Position = screenSpaceToClipSpace(pixelP0 - sideDisplacement0, viewportSize, p0.z, p0.w);
    EmitVertex();

    gColor        = vColor[1];
    gThickness    = vThickness[1];
    gLineDistance = totalHalfWidth1;
    gl_Position = screenSpaceToClipSpace(pixelP1 + sideDisplacement1, viewportSize, p1.z, p1.w);
    EmitVertex();

    gColor        = vColor[1];
    gThickness    = vThickness[1];
    gLineDistance = -totalHalfWidth1;
    gl_Position = screenSpaceToClipSpace( pixelP1 - sideDisplacement1, viewportSize, p1.z, p1.w);
    EmitVertex();

    EndPrimitive();
}