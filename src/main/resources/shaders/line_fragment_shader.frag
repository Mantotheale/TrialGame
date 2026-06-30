#version 330 core

in vec4 gColor;
noperspective in float gLineDistance;
noperspective in float gThickness;

out vec4 fragColor;

const float FRINGE = 1.5;

void main() {
    float absDistance = abs(gLineDistance);
    float halfWidth = gThickness * 0.5;

    float alpha = 1 - smoothstep(halfWidth, halfWidth + FRINGE, absDistance);
    fragColor = vec4(gColor.rgb, gColor.a * alpha);
}