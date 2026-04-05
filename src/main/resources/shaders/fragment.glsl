#version 330 core

in vec2 vertexUV;
out vec4 FragColor;

uniform sampler2D blockTexture;

void main() {
    FragColor = texture(blockTexture, vertexUV);
}