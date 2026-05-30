package com.gallatinapps.syntaxmp.engine.role

/**
 * A concrete styling role emitted for a token span.
 *
 * Known roles live on this namespace, such as [Keyword], [Keyword.Declaration], and
 * [Variable.Parameter]. Exact or custom role paths use [of].
 *
 * @property value Canonical dotted role path string (e.g. `"keyword.declaration"`). Also exposed
 * via [com.gallatinapps.syntaxmp.engine.spans.SyntaxTokenSpan.role].
 */
public sealed class SyntaxRole protected constructor(
    public val value: kotlin.String,
) {
    /** Returns this role plus [suffix] as a validated dotted child role. */
    public fun append(suffix: kotlin.String): SyntaxRole =
        of("$value.${normalizeRolePath(suffix, stripOuterDots = true)}")

    protected fun child(suffix: kotlin.String): SyntaxRole =
        Known("$value.${normalizeRolePath(suffix, stripOuterDots = true)}")

    /**
     * Returns this role's role chain from the root role through this exact role.
     *
     * For `constant.builtin.true`, this returns `constant`, `constant.builtin`, and
     * `constant.builtin.true`.
     */
    public fun rolesFromRoot(): List<SyntaxRole> =
        rolePathValuesFromRoot(value).map { of(it) }

    public final override fun equals(other: Any?): Boolean =
        other is SyntaxRole && value == other.value

    public final override fun hashCode(): Int = value.hashCode()

    public final override fun toString(): kotlin.String = value

    /** Grammar and control words. */
    public object Keyword : SyntaxRole("keyword") {
        public val Control: SyntaxRole = child("control")
        public val Declaration: SyntaxRole = child("declaration")
        public val Modifier: SyntaxRole = child("modifier")
        public val AtRule: SyntaxRole = child("at-rule")
    }

    /** String-like spans. */
    public object String : SyntaxRole("string") {
        public val Regex: SyntaxRole = child("regex")
        public val Language: SyntaxRole = child("language")
        public val Url: SyntaxRole = child("url")
    }

    /** Numeric spans. */
    public object Number : SyntaxRole("number")

    /** Comments. */
    public object Comment : SyntaxRole("comment")

    /** Callable names. */
    public object Function : SyntaxRole("function") {
        public val Builtin: SyntaxRole = child("builtin")
        public val Declaration: SyntaxRole = child("declaration")
        public val Member: SyntaxRole = child("member")
        public val Macro: SyntaxRole = child("macro")
    }

    /** Type-like names. */
    public object Type : SyntaxRole("type")

    /** Properties, fields, keys, and named configuration options. */
    public object Property : SyntaxRole("property") {
        public val Name: SyntaxRole = child("name")
        public val Quoted: SyntaxRole = child("quoted")
        public val Section: SyntaxRole = child("section")
    }

    /** Variables, parameters, placeholders, and namespaces. */
    public object Variable : SyntaxRole("variable") {
        public val Parameter: SyntaxRole = child("parameter")
        public val Namespace: SyntaxRole = child("namespace")
    }

    /** Operators. */
    public object Operator : SyntaxRole("operator")

    /** Structural punctuation. */
    public object Punctuation : SyntaxRole("punctuation") {
        public val Expression: SyntaxRole = child("expression")
    }

    /** Annotations, decorators, attributes, and metadata directives. */
    public object Annotation : SyntaxRole("annotation")

    /** Markup tags and tag-like selectors. */
    public object Tag : SyntaxRole("tag")

    /** Markup attributes and selector details. */
    public object Attribute : SyntaxRole("attribute") {
        public val Directive: SyntaxRole = child("directive")
        public val Pseudo: SyntaxRole = child("pseudo")
    }

    /** Constants and literal-like values. */
    public object Constant : SyntaxRole("constant") {
        public val Builtin: SyntaxRole = child("builtin")
        public val Color: SyntaxRole = child("color")
        public val Atom: SyntaxRole = child("atom")
    }

    /** Escapes and interpolation delimiters. */
    public object Escape : SyntaxRole("escape")

    /** Document, Markdown, component, and diff structural roles. */
    public object Markup : SyntaxRole("markup") {
        public val Expression: SyntaxRole = child("expression")
        public val Cdata: SyntaxRole = child("cdata")
        public val Frontmatter: SyntaxRole = child("frontmatter")
    }

    public companion object {
        /** Returns a validated exact/custom role path. */
        public fun of(value: kotlin.String): SyntaxRole {
            val normalized = normalizeRolePath(value, stripOuterDots = true)
            return knownRolesByValue[normalized] ?: Custom(normalized)
        }

        private val knownRolesByValue: Map<kotlin.String, SyntaxRole> by lazy {
            listOf(
                Keyword,
                Keyword.Control,
                Keyword.Declaration,
                Keyword.Modifier,
                Keyword.AtRule,
                String,
                String.Regex,
                String.Language,
                String.Url,
                Number,
                Comment,
                Function,
                Function.Builtin,
                Function.Declaration,
                Function.Member,
                Function.Macro,
                Type,
                Property,
                Property.Name,
                Property.Quoted,
                Property.Section,
                Variable,
                Variable.Parameter,
                Variable.Namespace,
                Operator,
                Punctuation,
                Punctuation.Expression,
                Annotation,
                Tag,
                Attribute,
                Attribute.Directive,
                Attribute.Pseudo,
                Constant,
                Constant.Builtin,
                Constant.Color,
                Constant.Atom,
                Escape,
                Markup,
                Markup.Expression,
                Markup.Cdata,
                Markup.Frontmatter,
            ).associateBy { it.value }
        }
    }

    private class Known(value: kotlin.String) : SyntaxRole(value)

    private class Custom(value: kotlin.String) : SyntaxRole(value)
}

internal fun rolePathValuesFromRoot(value: kotlin.String): List<kotlin.String> =
    buildList {
        var next = value
        add(next)
        while (next.contains('.')) {
            next = next.substringBeforeLast('.')
            add(next)
        }
    }.asReversed()

private fun normalizeRolePath(
    raw: kotlin.String,
    stripOuterDots: Boolean,
): kotlin.String {
    val normalized = raw
        .trim()
        .let { value -> if (stripOuterDots) value.trim('.') else value }
    require(normalized.isNotEmpty()) {
        "Syntax role must not be blank."
    }
    val segments = normalized.split('.')
    require(segments.none { it.isEmpty() }) {
        "Syntax role must not contain empty path segments: $raw"
    }
    require(segments.none { segment -> segment.any { it.isWhitespace() } }) {
        "Syntax role must not contain whitespace inside path segments: $raw"
    }
    return normalized
}
