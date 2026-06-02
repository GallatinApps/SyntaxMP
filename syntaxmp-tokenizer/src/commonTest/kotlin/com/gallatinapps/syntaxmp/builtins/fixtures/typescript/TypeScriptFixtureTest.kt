package com.gallatinapps.syntaxmp.builtins.fixtures.typescript

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class TypeScriptFixtureTest {
    private val code = """
        // ts scoring
        type Job = { name: string; enabled: boolean };
        interface Scorer { score(job: Job): number }
        const scorer = (job: Job): number => job.enabled ? 1 : 0;
    """.trimIndent()

    @Test
    fun `typescript comments declarations types functions properties and numbers`() {
        assertTokenAt("typescript", code, "// ts scoring", "Comment")
        assertTokenAt("typescript", code, "type", "Keyword")
        assertTokenAt("typescript", code, "Job", "Type")
        assertTokenAt("typescript", code, "name", "Property")
        assertTokenAt("typescript", code, "string", "Variable")
        assertTokenAt("typescript", code, "interface", "Keyword")
        assertTokenAt("typescript", code, "Scorer", "Type")
        assertTokenAt("typescript", code, "score", "Function")
        assertTokenAt("typescript", code, "const", "Keyword")
        assertTokenAt("typescript", code, "enabled", "Property", occurrence = 1)
        assertTokenAt("typescript", code, "1", "Number")
        assertTokenAt("typescript", code, "0", "Number")
    }

    @Test
    fun `keywords inside strings are not highlighted`() = assertNoTokenAt(
        language = "typescript",
        code = "const label = \"interface\";",
        substring = "interface",
        category = "Keyword",
    )

    @Test
    fun `template string interpolation tokenizes expression`() {
        val code = "const label = `score ${'$'}{job.count}`;"

        assertTokenAt("typescript", code, "`score ", "String")
        assertTokenAt("typescript", code, "${'$'}{", "Escape")
        assertTokenAt("typescript", code, "job", "Variable")
        assertTokenAt("typescript", code, "count", "Property")
        assertTokenAt("typescript", code, "}", "Escape")
    }

    @Test
    fun `typescript type only imports enums generics satisfies as and spread are covered`() {
        val code = """
            import type { Job } from "./types";
            export type Result<T> = { value: T; ok?: boolean };
            export interface Repository<T> { find(id: string): Promise<T> }
            export class Store<T> {}
            export enum Status { Draft, Review = 2 }
            const result = { value: job, ok: true } satisfies Result<Job>;
            const cast = result as Result<Job>;
            const found = repo.find?.(id) ?? /missing/.test(label);
            const clone = { ...result };
        """.trimIndent()

        assertTokenAt("typescript", code, "import", "Keyword")
        assertTokenAt("typescript", code, "type", "Keyword")
        assertTokenAt("typescript", code, "Job", "Type")
        assertTokenAt("typescript", code, "from", "Keyword")
        assertTokenAt("typescript", code, "export", "Keyword")
        assertTokenAt("typescript", code, "Result", "Type")
        assertTokenAt("typescript", code, "T", "Type")
        assertTokenAt("typescript", code, "value", "Property")
        assertTokenAt("typescript", code, "interface", "Keyword")
        assertTokenAt("typescript", code, "Repository", "Type")
        assertTokenAt("typescript", code, "find", "Function")
        assertTokenAt("typescript", code, "class", "Keyword")
        assertTokenAt("typescript", code, "Store", "Type")
        assertTokenAt("typescript", code, "enum", "Keyword")
        assertTokenAt("typescript", code, "Status", "Type")
        assertTokenAt("typescript", code, "Draft", "Constant")
        assertTokenAt("typescript", code, "Review", "Constant")
        assertTokenAt("typescript", code, "satisfies", "Keyword")
        assertTokenAt("typescript", code, "as", "Keyword", occurrence = 2)
        assertTokenAt("typescript", code, "find", "Property", occurrence = 1)
        assertTokenAt("typescript", code, "??", "Operator")
        assertTokenAt("typescript", code, "/missing/", "String", "string.regex")
        assertTokenAt("typescript", code, "test", "Function")
        assertTokenAt("typescript", code, "...", "Punctuation")
    }

    @Test
    fun `typescript colon properties are context aware`() {
        val code = """
            type Props = {
              title: string;
              count?: number;
              nested: { enabled: boolean };
            };

            interface Repo {
              find(id: string): Result;
              readonly size: number;
            }

            interface Result {}

            const value = { title: "Hello", count: 1 } satisfies Props;

            let title: string = "Hello";
            function render(title: string, count?: number): void {}
            const mapper = (title: string) => title;
        """.trimIndent()

        assertTokenAt("typescript", code, "title", "Property")
        assertTokenAt("typescript", code, "count", "Property")
        assertTokenAt("typescript", code, "nested", "Property")
        assertTokenAt("typescript", code, "enabled", "Property")
        assertTokenAt("typescript", code, "find", "Function")
        assertTokenAt("typescript", code, "id", "Variable")
        assertNoTokenAt("typescript", code, "id", "Property")
        assertTokenAt("typescript", code, "Result", "Type")
        assertTokenAt("typescript", code, "readonly", "Keyword")
        assertTokenAt("typescript", code, "size", "Property")
        assertTokenAt("typescript", code, "title", "Property", occurrence = 1)
        assertTokenAt("typescript", code, "count", "Property", occurrence = 1)
        assertTokenAt("typescript", code, "satisfies", "Keyword")
        assertTokenAt("typescript", code, "title", "Variable", occurrence = 2)
        assertNoTokenAt("typescript", code, "title", "Property", occurrence = 2)
        assertTokenAt("typescript", code, "title", "Variable", occurrence = 3)
        assertTokenAt("typescript", code, "count", "Variable", occurrence = 2)
        assertNoTokenAt("typescript", code, "count", "Property", occurrence = 2)
        assertTokenAt("typescript", code, "title", "Variable", occurrence = 4)
        assertNoTokenAt("typescript", code, "title", "Property", occurrence = 4)
    }
}
