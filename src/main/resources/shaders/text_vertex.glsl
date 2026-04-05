#version 330 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aUV;

out vec2 vertexUV;

uniform vec2 screenSize;

void main() {
    vec2 ndc = vec2(
        (aPos.x / screenSize.x) * 2.0 - 1.0,
        1.0 - (aPos.y / screenSize.y) * 2.0
    );

    vertexUV = aUV;
    gl_Position = vec4(ndc, 0.0, 1.0);
}