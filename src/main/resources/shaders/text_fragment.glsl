#version 330 core

in vec2 vertexUV;
out vec4 FragColor;

uniform sampler2D fontTexture;
uniform vec3 textColor;

void main() {
    vec4 sampled = texture(fontTexture, vertexUV);

    if (sampled.a < 0.1) {
        discard;
    }

    FragColor = vec4(textColor, sampled.a);
}