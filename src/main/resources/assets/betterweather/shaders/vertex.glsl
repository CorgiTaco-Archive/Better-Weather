#version 330

layout (location = 0) in vec3 position;

uniform mat4 pos;

void main() {
    gl_Position = pos * vec4(position, 1.0);
}
