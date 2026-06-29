#version 330

flat in int texIdx;
in vec2 texCoord;

out vec4 FragColor;

uniform sampler2D tex[16];

void main() {
    FragColor = texture(tex[texIdx], texCoord);
}