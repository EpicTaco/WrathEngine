#version 400 core

in vec2 pass_TextureCoord;
in vec3 pass_SurfNormal;
in vec3 pass_ToLightVector;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform vec3 lightColor = vec3(1.0, 1.0, 1.0);

void main(void)
{
	vec3 normNorm = normalize(pass_SurfNormal);
	vec3 normLight = normalize(pass_ToLightVector);
	vec3 diffuse = max(dot(normNorm, normLight), 0.0) * lightColor;
    out_Color = vec4(diffuse, 1.0) * texture(textureSampler, pass_TextureCoord);
}