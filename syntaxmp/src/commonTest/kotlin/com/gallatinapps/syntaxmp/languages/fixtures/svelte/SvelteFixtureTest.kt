package com.gallatinapps.syntaxmp.languages.fixtures.svelte

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class SvelteFixtureTest {
    private val code = """
        <script lang="ts">
        let count: number = 0;
        </script>
        {#if count > 0}
          <button on:click={() => count += 1}>{count}</button>
        {/if}
        <style>
        .active { color: transparent; }
        </style>
    """.trimIndent()

    @Test
    fun `svelte script blocks directives expressions and style`() {
        assertTokenAt("svelte", code, "script", "Tag")
        assertTokenAt("svelte", code, "lang", "Attribute")
        assertTokenAt("svelte", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("svelte", code, "count", "Variable")
        assertTokenAt("svelte", code, "number", "Variable")
        assertTokenAt("svelte", code, "0", "Number")
        assertTokenAt("svelte", code, "{#if", "Keyword", "keyword")
        assertTokenAt("svelte", code, "count", "Variable", occurrence = 1)
        assertTokenAt("svelte", code, "button", "Tag")
        assertTokenAt("svelte", code, "on:click", "Attribute", "attribute.directive")
        assertTokenAt("svelte", code, "count", "Variable", occurrence = 2)
        assertTokenAt("svelte", code, "1", "Number")
        assertTokenAt("svelte", code, "{/if", "Keyword", "keyword")
        assertTokenAt("svelte", code, "style", "Tag")
        assertTokenAt("svelte", code, ".active", "Attribute")
        assertTokenAt("svelte", code, "color", "Property")
        assertTokenAt("svelte", code, "transparent", "Constant", "constant.builtin.transparent")
    }

    @Test
    fun `svelte mixed script template string blocks directives entities and style`() {
        val code = """
            <script lang="ts">
            let count: number = 1;
            const label = `Count ${'$'}{count}`;
            </script>
            {#if count > 0}
              <button on:click={() => count += 1} title="A &amp; B">{label}</button>
            {:else}
              <p>Empty</p>
            {/if}
            <style>
            .button { color: transparent; }
            </style>
        """.trimIndent()

        assertTokenAt("svelte", code, "script", "Tag")
        assertTokenAt("svelte", code, "\"ts\"", "String")
        assertTokenAt("svelte", code, "let", "Keyword", "keyword.declaration")
        assertTokenAt("svelte", code, "number", "Variable")
        assertTokenAt("svelte", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("svelte", code, "`Count ", "String")
        assertTokenAt("svelte", code, "${'$'}{", "Escape")
        assertTokenAt("svelte", code, "{#if", "Keyword", "keyword")
        assertTokenAt("svelte", code, "button", "Tag")
        assertTokenAt("svelte", code, "on:click", "Attribute", "attribute.directive")
        assertTokenAt("svelte", code, "count", "Variable", occurrence = 2)
        assertTokenAt("svelte", code, "title", "Attribute")
        assertTokenAt("svelte", code, "&amp;", "Escape")
        assertTokenAt("svelte", code, "label", "Variable", occurrence = 1)
        assertTokenAt("svelte", code, "{:else", "Keyword", "keyword")
        assertTokenAt("svelte", code, "{/if", "Keyword", "keyword")
        assertTokenAt("svelte", code, "style", "Tag")
        assertTokenAt("svelte", code, ".button", "Attribute")
        assertTokenAt("svelte", code, "transparent", "Constant", "constant.builtin.transparent")
    }

    @Test
    fun `svelte each blocks reactive labels ts script and style keep boundary roles`() {
        val code = """
            <script lang="ts">
            type Item = { title: string };
            let items: Item[] = [];
            $: total = items.length;
            </script>
            {#each items as item}
              <button on:click={() => select(item)}>{item.title}</button>
            {/each}
            <style>
            .item { color: red; }
            </style>
        """.trimIndent()

        assertTokenAt("svelte", code, "script", "Tag")
        assertTokenAt("svelte", code, "\"ts\"", "String")
        assertTokenAt("svelte", code, "type", "Keyword", "keyword.declaration")
        assertTokenAt("svelte", code, "Item", "Type")
        assertTokenAt("svelte", code, "${'$'}:", "Keyword", "keyword")
        assertTokenAt("svelte", code, "{#each", "Keyword", "keyword")
        assertTokenAt("svelte", code, "button", "Tag")
        assertTokenAt("svelte", code, "on:click", "Attribute", "attribute.directive")
        assertTokenAt("svelte", code, "select", "Function")
        assertTokenAt("svelte", code, "item", "Variable", occurrence = 4)
        assertTokenAt("svelte", code, "title", "Property", occurrence = 1)
        assertTokenAt("svelte", code, "{/each", "Keyword", "keyword")
        assertTokenAt("svelte", code, "style", "Tag")
        assertTokenAt("svelte", code, "color", "Property")
    }

    @Test
    fun `svelte typed untyped style and comment boundaries do not leak`() {
        val code = """
            <script>let plain = 1;</script>
            <script lang="ts">interface Item { title: string }</script>
            <style>.plain { color: red; }</style>
            <style lang="scss">${'$'}accent: red; .typed { color: ${'$'}accent; }</style>
            <p>let outside = 2;</p>
            <!-- on:click={outside} -->
        """.trimIndent()

        assertTokenAt("svelte", code, "plain", "Variable")
        assertTokenAt("svelte", code, "\"ts\"", "String")
        assertTokenAt("svelte", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("svelte", code, "Item", "Type")
        assertTokenAt("svelte", code, ".plain", "Attribute")
        assertTokenAt("svelte", code, "color", "Property")
        assertTokenAt("svelte", code, "\"scss\"", "String")
        assertTokenAt("svelte", code, "${'$'}accent", "Variable")
        assertTokenAt("svelte", code, "${'$'}accent", "Variable", occurrence = 1)
        assertNoTokenAt("svelte", code, "outside", "Variable")
        assertNoTokenAt("svelte", code, "on:click", "Attribute")
    }
}
