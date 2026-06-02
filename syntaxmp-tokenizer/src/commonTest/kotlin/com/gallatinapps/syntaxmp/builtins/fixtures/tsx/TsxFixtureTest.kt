package com.gallatinapps.syntaxmp.builtins.fixtures.tsx

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class TsxFixtureTest {
    private val code = """
        type Props = { title: string; count?: number };

        export function Panel(props: Props) {
          return <PanelView title={props.title} count={props.count} />;
        }
    """.trimIndent()

    @Test
    fun `tsx script types markup attributes and expressions`() {
        assertTokenAt("tsx", code, "type", "Keyword", "keyword.declaration")
        assertTokenAt("tsx", code, "Props", "Type")
        assertTokenAt("tsx", code, "string", "Variable")
        assertTokenAt("tsx", code, "number", "Variable")
        assertTokenAt("tsx", code, "export", "Keyword", "keyword.declaration")
        assertTokenAt("tsx", code, "Panel", "Function")
        assertTokenAt("tsx", code, "props", "Variable")
        assertTokenAt("tsx", code, "PanelView", "Tag")
        assertTokenAt("tsx", code, "title", "Attribute", occurrence = 1)
        assertTokenAt("tsx", code, "count", "Attribute", occurrence = 1)
        assertTokenAt("tsx", code, "props", "Variable", occurrence = 1)
        assertTokenAt("tsx", code, "title", "Property", occurrence = 2)
        assertTokenAt("tsx", code, "count", "Property", occurrence = 2)
    }

    @Test
    fun `tsx mixed types template strings markup expressions and entities`() {
        val code = """
            type Props = { title: string; count: number };
            const summary = `Count ${'$'}{props.count}`;

            export function View(props: Props) {
              return <PanelView title={summary} data-copy="Rows &amp; totals">{props.count}</PanelView>;
            }
        """.trimIndent()

        assertTokenAt("tsx", code, "type", "Keyword", "keyword.declaration")
        assertTokenAt("tsx", code, "Props", "Type")
        assertTokenAt("tsx", code, "summary", "Variable")
        assertTokenAt("tsx", code, "`Count ", "String")
        assertTokenAt("tsx", code, "${'$'}{", "Escape")
        assertTokenAt("tsx", code, "props", "Variable")
        assertTokenAt("tsx", code, "count", "Property", occurrence = 1)
        assertTokenAt("tsx", code, "View", "Function")
        assertTokenAt("tsx", code, "PanelView", "Tag")
        assertTokenAt("tsx", code, "title", "Attribute", occurrence = 1)
        assertTokenAt("tsx", code, "summary", "Variable", occurrence = 1)
        assertTokenAt("tsx", code, "data-copy", "Attribute")
        assertTokenAt("tsx", code, "&amp;", "Escape")
        assertTokenAt("tsx", code, "props", "Variable", occurrence = 2)
        assertTokenAt("tsx", code, "count", "Property", occurrence = 2)
    }

    @Test
    fun `tsx generic component tags event props and expression braces stay in tsx boundary`() {
        val code = """
            interface Item { name: string }
            const items: Item[] = [];
            const view = <ListView<Item> items={items} renderItem={(item) => item.name} />;
        """.trimIndent()

        assertTokenAt("tsx", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("tsx", code, "Item", "Type")
        assertTokenAt("tsx", code, "ListView", "Tag")
        assertTokenAt("tsx", code, "Item", "Type", occurrence = 2)
        assertTokenAt("tsx", code, "items", "Attribute", occurrence = 1)
        assertTokenAt("tsx", code, "{", "Punctuation", occurrence = 1)
        assertTokenAt("tsx", code, "items", "Variable", occurrence = 2)
        assertTokenAt("tsx", code, "renderItem", "Attribute")
        assertTokenAt("tsx", code, "item", "Variable", occurrence = 3)
        assertTokenAt("tsx", code, "name", "Property", occurrence = 1)
    }

    @Test
    fun `tsx raw text closes and resumes typescript after markup`() {
        val code = """
            const view = (
              <>
                <script lang="ts">interface Inner { id: string }</script>
                <p># not markdown</p>
              </>
            );
            interface After { ok: boolean }
        """.trimIndent()

        assertTokenAt("tsx", code, "view", "Variable")
        assertTokenAt("tsx", code, "\"ts\"", "String")
        assertTokenAt("tsx", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("tsx", code, "Inner", "Type")
        assertTokenAt("tsx", code, "After", "Type")
        assertNoTokenAt("tsx", code, "#", "Markup")
    }
}
