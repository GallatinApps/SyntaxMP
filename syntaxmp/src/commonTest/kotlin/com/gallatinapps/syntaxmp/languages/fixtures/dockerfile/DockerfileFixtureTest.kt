package com.gallatinapps.syntaxmp.languages.fixtures.dockerfile

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class DockerfileFixtureTest {
    private val code = """
        # Runtime image
        FROM --platform=${'$'}TARGETPLATFORM alpine:3.20 AS runtime
        ARG APP_HOME=/app
        ENV ENABLED=true
        COPY --from=builder /out/app ${'$'}APP_HOME/app
        RUN echo "ready" && chmod +x ${'$'}APP_HOME/app
    """.trimIndent()

    @Test
    fun `dockerfile instructions flags variables strings and comments`() {
        assertTokenAt("dockerfile", code, "# Runtime image", "Comment")
        assertTokenAt("dockerfile", code, "FROM", "Keyword")
        assertTokenAt("dockerfile", code, "--platform", "Attribute")
        assertTokenAt("dockerfile", code, "${'$'}TARGETPLATFORM", "Variable")
        assertTokenAt("dockerfile", code, "ARG", "Keyword", occurrence = 1)
        assertTokenAt("dockerfile", code, "ENV", "Keyword")
        assertTokenAt("dockerfile", code, "COPY", "Keyword")
        assertTokenAt("dockerfile", code, "--from", "Attribute")
        assertTokenAt("dockerfile", code, "RUN", "Keyword")
        assertTokenAt("dockerfile", code, "\"ready\"", "String")
    }

    @Test
    fun `instruction-looking text inside a string is not a keyword`() = assertNoTokenAt(
        language = "dockerfile",
        code = code,
        substring = "ready",
        category = "Keyword",
    )

    @Test
    fun `generic run and copy heredoc bodies stay strings`() {
        val code = """
            RUN cat <<EOF > /tmp/message.txt
            echo FROM inside
            EOF
            COPY <<'EOF' /app/config
            ENV inside
            EOF
        """.trimIndent()

        assertTokenAt("dockerfile", code, "RUN", "Keyword")
        assertTokenAt("dockerfile", code, "<<EOF > /tmp/message.txt\necho FROM inside\nEOF", "String")
        assertNoTokenAt("dockerfile", code, "FROM", "Keyword")
        assertTokenAt("dockerfile", code, "COPY", "Keyword")
        assertTokenAt("dockerfile", code, "<<'EOF' /app/config\nENV inside\nEOF", "String")
        assertNoTokenAt("dockerfile", code, "ENV", "Keyword")
    }

    @Test
    fun `json array forms and quoted comment markers keep argument boundaries`() {
        val code = """
            CMD ["app", "--serve", "#not-comment"]
            RUN echo "# still string" --flag=value # real comment
        """.trimIndent()

        assertTokenAt("dockerfile", code, "CMD", "Keyword")
        assertTokenAt("dockerfile", code, "[", "Punctuation")
        assertTokenAt("dockerfile", code, "\"app\"", "String")
        assertTokenAt("dockerfile", code, "\"--serve\"", "String")
        assertTokenAt("dockerfile", code, "\"#not-comment\"", "String")
        assertNoTokenAt("dockerfile", code, "#not-comment", "Comment")
        assertTokenAt("dockerfile", code, "RUN", "Keyword")
        assertTokenAt("dockerfile", code, "\"# still string\"", "String")
        assertNoTokenAt("dockerfile", code, "# still string", "Comment")
        assertTokenAt("dockerfile", code, "--flag", "Attribute")
        assertTokenAt("dockerfile", code, "# real comment", "Comment")
    }

    @Test
    fun `run shell heredoc bodies route through shell tokenizer`() {
        val code = """
            FROM alpine
            RUN <<EOF
            set -e
            echo "${'$'}HOME"
            apk add --no-cache curl
            EOF
            RUN <<-EOF
            ${'\t'}echo "${'$'}HOME"
            EOF
            RUN <<'EOF'
            echo "${'$'}HOME"
            EOF
            RUN <<EOF bash
            echo "${'$'}HOME"
            EOF
            RUN bash <<EOF
            echo "${'$'}HOME"
            EOF
            RUN cat <<EOF > /tmp/message.txt
            hello "${'$'}HOME"
            EOF
            COPY <<EOF /tmp/message.txt
            hello "${'$'}HOME"
            EOF
        """.trimIndent()

        assertTokenAt("dockerfile", code, "FROM", "Keyword")
        assertTokenAt("dockerfile", code, "<<EOF", "String")
        assertTokenAt("dockerfile", code, "set", "Function")
        assertTokenAt("dockerfile", code, "${'$'}HOME", "Variable")
        assertTokenAt("dockerfile", code, "<<-EOF", "String")
        assertTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 1)
        assertTokenAt("dockerfile", code, "<<'EOF'", "String")
        assertTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 2)
        assertTokenAt("dockerfile", code, "<<EOF bash", "String")
        assertTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 3)
        assertTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 4)
        assertTokenAt(
            language = "dockerfile",
            code = code,
            substring = "<<EOF > /tmp/message.txt\nhello \"${'$'}HOME\"\nEOF",
            category = "String",
        )
        assertNoTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 5)
        assertTokenAt(
            language = "dockerfile",
            code = code,
            substring = "<<EOF /tmp/message.txt\nhello \"${'$'}HOME\"\nEOF",
            category = "String",
        )
        assertNoTokenAt("dockerfile", code, "${'$'}HOME", "Variable", occurrence = 6)
        assertNoTokenAt("dockerfile", code, "EOF", "Variable")
    }
}
