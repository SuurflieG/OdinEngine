#version 330 core

in vec2 vTexCoord;
out vec4 FragColor;

uniform sampler2D fontSampler;
uniform vec3 textColor;

void main() {
    float alpha = texture(fontSampler, vTexCoord).r;
    FragColor = vec4(textColor, alpha);
}