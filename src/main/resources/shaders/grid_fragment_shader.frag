#version 330

in vec2 vWorldPos;
out vec4 FragColor;

void main() {
    vec2 f = fract(vWorldPos);
    vec2 dist = min(f, 1.0 - f);
    vec2 pixelSize = fwidth(vWorldPos);
    vec2 gridLines = 1.0 - smoothstep(vec2(0.0), pixelSize, dist);
    float alpha = max(gridLines.x, gridLines.y);
    FragColor = vec4(0.92, 0.92, 0.92, alpha);
}