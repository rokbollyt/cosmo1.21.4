#version 150

#moj_import <mre:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float GlassDirection;
uniform float GlassQuality;
uniform float GlassSize;

out vec4 OutColor;

const float PI = 3.14159265;

vec4 liquidGlassBlur(sampler2D tex, vec2 uv, float direction, float quality, float size) {
    vec2 R = vec2(textureSize(tex, 0));
    vec2 radius = size / R;
    vec4 color = texture(tex, uv);
    for (float d = 0.0; d < PI; d += PI / direction) {
        for (float i = 1.0 / quality; i <= 1.0; i += 1.0 / quality) {
            color += texture(tex, uv + vec2(cos(d), sin(d)) * radius * i);
        }
    }
    color /= (quality * direction);
    return color;
}

void main() {
    vec2 halfSize = Size / 2.0;
    vec2 pos = halfSize - FragCoord * Size;
    float sdf = rdist(pos, halfSize - 1.0, Radius);
    float boxShape = smoothstep(Smoothness * 2.0, 0.0, sdf);
    if (boxShape < 0.01) {
        discard;
    }
    float min_half = min(halfSize.x, halfSize.y);
    float normalized_depth = abs(sdf) / min_half;
    float edgeBlendFactor = pow(1.0 - normalized_depth, GlassDirection);
    float scale = 0.5 + 0.4 * smoothstep(0.5, 1.0, 1.0 - normalized_depth);
    vec2 res = vec2(textureSize(Sampler0, 0));
    vec2 offset = (FragCoord - 0.5) * (Size / res) * (scale - 1.0);
    vec2 distorted_uv = TexCoord + offset;
    vec4 blurred = liquidGlassBlur(Sampler0, distorted_uv, GlassDirection, GlassQuality, GlassSize);
    vec3 glass_col = vec3(0.2) + blurred.rgb * 0.7;
    vec3 background_col = texture(Sampler0, TexCoord).rgb * 0.8;
    vec3 col = mix(background_col, glass_col, boxShape);
    vec2 grad = normalize(vec2(dFdx(sdf), dFdy(sdf)));
    float hl = clamp((1.0 - pow(1.0 - abs(dot(grad, normalize(vec2(-1.0, 1.0)))), 0.5)) * pow(edgeBlendFactor, 5.0), 0.0, 1.0);
    col += 0.5 * hl;
    OutColor = vec4(col, boxShape) * FragColor;
}