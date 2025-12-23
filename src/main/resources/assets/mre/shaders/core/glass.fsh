#version 150

#moj_import <mre:common.glsl>

in vec4 FragColor;
in vec2 FragCoord;
in vec2 TexCoord;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform float GlassOffset;
uniform float Smoothness;
uniform vec4 Radius;

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;

    vec2 dir = (FragCoord * Size - center) / max(Size.x, Size.y);
    vec2 glassSize = (Size / max(Size.x, Size.y)) * 0.5;
    float dist = rdist(dir, glassSize, Radius / max(Size.x, Size.y));

    float mask = smoothstep(0.0, 0.0, dist);
    float mask2 = smoothstep(-GlassOffset, 0.0, dist);

    vec2 offset = normalize(dir) * pow(mask2, 5.0) * 0.1;

    vec4 r = texture(Sampler0, TexCoord * 0.9985 + offset);
    vec4 g = texture(Sampler0, TexCoord * 1.0015 + offset);
    vec4 b = texture(Sampler0, TexCoord + offset);

    vec4 mixed = vec4(r.r, g.g, b.b, 1.0) + vec4(0.2 * pow(mask2, 2.0) + 0.1);

    vec4 base = texture(Sampler0, TexCoord);
    vec4 final = mix(mixed, base, mask);

    final *= FragColor;
    final.a *= ralpha(Size, FragCoord, Radius, Smoothness);

    if (final.a == 0.0) discard;

    OutColor = final;
}
