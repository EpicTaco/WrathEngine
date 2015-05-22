#version 400 core

in vec3 in_Normals;
in vec3 in_Position;
in vec2 in_TextureCoord;

out vec2 pass_TextureCoord;
out vec3 pass_SurfNormal;
out vec3 pass_ToLightVector;

uniform mat4 transformationMatrix = mat4(1.0);
uniform mat4 projectionMatrix = mat4(1.0);
uniform mat4 viewMatrix = mat4(1.0);
uniform vec3 lightPosition = vec3(0.0, 10.0, 0.0);

void main(void)
{
	vec4 position = transformationMatrix * vec4(in_Position, 1.0);
    gl_Position = viewMatrix * projectionMatrix * position;
	pass_TextureCoord = in_TextureCoord;
	
	pass_SurfNormal = (transformationMatrix * vec4(in_Normals, 0.0)).xyz;
	pass_ToLightVector = lightPosition - position.xyz;
}