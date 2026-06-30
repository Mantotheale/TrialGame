#version 330 core

in vec4 gColor;
in float gLineDistance;

out vec4 fragColor;

uniform float lineWidth;

void main() {
    float absDistance = abs(gLineDistance);
    float halfWidth = lineWidth * 0.5;

    float alpha = 1 - smoothstep(halfWidth, halfWidth + 1, absDistance);
    fragColor = vec4(gColor.rgb, gColor.a * alpha);
}