#version 330 core

in vec2 gUV;
flat in vec4 gColor;

out vec4 fColor;

void main() {
    float dist = length(gUV);
    float pixelSize = fwidth(dist);
    float alpha = 1 - smoothstep(1 - pixelSize, 1, dist);
    fColor = vec4(gColor.rgb, gColor.a * alpha);
}