package com.gallatinapps.syntaxmp.languages.fixtures.glsl

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class GlslFixtureTest {
    private val code = """
        #version 300 es
        precision mediump float;
        uniform sampler2D tex;
        in vec2 uv;
        out vec4 fragColor;
        void main() {
          vec3 color = texture(tex, uv).rgb;
          fragColor = vec4(mix(color, vec3(1.0), 0.5), 1.0);
        }
    """.trimIndent()

    @Test
    fun `glsl preprocessor declarations vector types builtins and numbers`() {
        assertTokenAt("glsl", code, "#version 300 es", "Annotation")
        assertTokenAt("glsl", code, "precision", "Keyword")
        assertTokenAt("glsl", code, "float", "Type")
        assertTokenAt("glsl", code, "uniform", "Keyword")
        assertTokenAt("glsl", code, "sampler2D", "Type")
        assertTokenAt("glsl", code, "vec2", "Type")
        assertTokenAt("glsl", code, "vec4", "Type")
        assertTokenAt("glsl", code, "void", "Type")
        assertTokenAt("glsl", code, "main", "Function")
        assertTokenAt("glsl", code, "texture", "Function")
        assertTokenAt("glsl", code, "mix", "Function")
        assertTokenAt("glsl", code, "1.0", "Number")
    }

    @Test
    fun `keywords inside comments are not highlighted`() = assertNoTokenAt(
        language = "glsl",
        code = "// uniform vec3",
        substring = "uniform",
        category = "Keyword",
    )

    @Test
    fun `glsl comments layout swizzles and operators keep roles`() {
        val code = """
            // uniform vec3 ignored
            /* return vec4 ignored */
            layout(location = 0) in vec3 position;
            vec4 color = texture(tex, uv);
            float r = color.rgb.r + .5;
        """.trimIndent()

        assertTokenAt("glsl", code, "// uniform vec3 ignored", "Comment")
        assertTokenAt("glsl", code, "/* return vec4 ignored */", "Comment")
        assertTokenAt("glsl", code, "layout", "Keyword")
        assertTokenAt("glsl", code, "in", "Keyword")
        assertTokenAt("glsl", code, "vec3", "Type", occurrence = 1)
        assertTokenAt("glsl", code, "position", "Variable")
        assertTokenAt("glsl", code, "texture", "Function")
        assertTokenAt("glsl", code, "rgb", "Property")
        assertTokenAt("glsl", code, ".5", "Number")
        assertNoTokenAt("glsl", code, "uniform", "Keyword")
        assertNoTokenAt("glsl", code, "vec4", "Type")
    }
}
