package com.gallatinapps.syntaxmp.languages.glsl

import com.gallatinapps.syntaxmp.engine.primitives.lexemeRoleMap
import com.gallatinapps.syntaxmp.engine.role.SyntaxRole

internal val GlslKeywordRoles = lexemeRoleMap(
    SyntaxRole.Keyword to setOf(
        "attribute", "centroid", "default", "discard", "flat", "highp", "in", "inout", "invariant",
        "layout", "lowp", "mediump", "noperspective", "out", "patch", "precision", "sample", "smooth",
        "subroutine", "uniform", "varying",
    ),
    SyntaxRole.Keyword.Control to setOf("break", "case", "continue", "do", "else", "for", "if", "return", "switch", "while"),
    SyntaxRole.Keyword.Declaration to setOf("const", "struct"),
)
internal val GlslConstants = setOf("false", "true")
internal val GlslBuiltinRoles = lexemeRoleMap(
    SyntaxRole.Function.Builtin to setOf(
        "abs", "acos", "asin", "atan", "ceil", "clamp", "cos", "cross", "degrees", "distance", "dot",
        "exp", "floor", "fract", "length", "mix", "normalize", "pow", "radians", "reflect", "sin",
        "smoothstep", "sqrt", "step", "tan", "texture", "texture2D",
    ),
)
internal val GlslTypeKeywords = setOf(
    "bool", "bvec2", "bvec3", "bvec4", "double", "dmat2", "dmat3", "dmat4", "dvec2", "dvec3",
    "dvec4", "float", "int", "ivec2", "ivec3", "ivec4", "mat2", "mat3", "mat4", "sampler2D",
    "samplerCube", "uint", "uvec2", "uvec3", "uvec4", "vec2", "vec3", "vec4", "void",
)
