package com.gallatinapps.syntaxmp.demo.model.samples

internal val GlslSample = """
    #version 300 es
    precision highp float;

    #define GLOW_STRENGTH 0.18

    uniform sampler2D noteTexture;
    uniform vec2 viewportSize;
    uniform float exposure;
    in vec2 vUv;
    out vec4 fragColor;

    vec3 tonemap(vec3 color) {
        return color / (color + vec3(1.0));
    }

    void main() {
        vec2 px = 1.0 / viewportSize;
        vec3 base = texture(noteTexture, vUv).rgb;
        vec3 glow = texture(noteTexture, vUv + px * 2.0).rgb;
        vec3 color = mix(base, glow, GLOW_STRENGTH) * exposure;
        fragColor = vec4(tonemap(color), 1.0);
    }
""".trimIndent()
