#version 400 core

in vec3 in_Position;
in vec2 in_TextureCoord;

out vec2 pass_TextureCoord;

void main(void)
{
    gl_Position = vec4(in_Position, 1.0);
	pass_TextureCoord = in_TextureCoord;
}