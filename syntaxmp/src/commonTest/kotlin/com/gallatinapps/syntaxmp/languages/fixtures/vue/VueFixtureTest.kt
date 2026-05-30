package com.gallatinapps.syntaxmp.languages.fixtures.vue

import com.gallatinapps.syntaxmp.languages.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.languages.fixtures.assertTokenAt
import kotlin.test.Test

class VueFixtureTest {
    private val code = """
        <template>
          <section :class="{ active: count > 0 }" @click="count++">{{ count }}</section>
        </template>
        <script setup lang="ts">
        import { ref } from "vue";
        const count: number = ref(0);
        </script>
        <style lang="scss">
        .note { color: ${'$'}accent; }
        </style>
    """.trimIndent()

    @Test
    fun `vue template directives moustache script and style`() {
        assertTokenAt("vue", code, "template", "Tag")
        assertTokenAt("vue", code, "section", "Tag")
        assertTokenAt("vue", code, ":class", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "\"{ active: count > 0 }\"", "String")
        assertTokenAt("vue", code, "@click", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "\"count++\"", "String")
        assertTokenAt("vue", code, "{{", "Markup", "markup.expression")
        assertTokenAt("vue", code, "count", "Variable", occurrence = 2)
        assertTokenAt("vue", code, "script", "Tag")
        assertTokenAt("vue", code, "setup", "Attribute")
        assertTokenAt("vue", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "ref", "Function", occurrence = 1)
        assertTokenAt("vue", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "number", "Variable")
        assertTokenAt("vue", code, "style", "Tag")
        assertTokenAt("vue", code, ".note", "Attribute")
        assertTokenAt("vue", code, "color", "Property")
        assertTokenAt("vue", code, "${'$'}accent", "Variable")
    }

    @Test
    fun `vue mixed template entities script setup ts and scss interpolation`() {
        val code = """
            <template>
              <PanelCard :title="label" @click="count++">Total &amp; {{ count }}</PanelCard>
            </template>
            <script setup lang="ts">
            import { ref } from "vue";
            const count = ref(1);
            const label = `Count ${'$'}{count.value}`;
            </script>
            <style lang="scss">
            .card-#{${'$'}state} { content: "Count #{${'$'}state}"; }
            </style>
        """.trimIndent()

        assertTokenAt("vue", code, "PanelCard", "Tag")
        assertTokenAt("vue", code, ":title", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "\"label\"", "String")
        assertTokenAt("vue", code, "@click", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "&amp;", "Escape")
        assertTokenAt("vue", code, "{{", "Markup", "markup.expression")
        assertTokenAt("vue", code, "count", "Variable", occurrence = 1)
        assertTokenAt("vue", code, "script", "Tag")
        assertTokenAt("vue", code, "setup", "Attribute")
        assertTokenAt("vue", code, "\"ts\"", "String")
        assertTokenAt("vue", code, "import", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "const", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "`Count ", "String")
        assertTokenAt("vue", code, "${'$'}{", "Escape")
        assertTokenAt("vue", code, "value", "Property")
        assertTokenAt("vue", code, "style", "Tag")
        assertTokenAt("vue", code, "\"scss\"", "String")
        assertTokenAt("vue", code, "#{", "Escape")
        assertTokenAt("vue", code, "${'$'}state", "Variable")
        assertTokenAt("vue", code, "#{", "Escape", occurrence = 1)
        assertTokenAt("vue", code, "${'$'}state", "Variable", occurrence = 1)
    }

    @Test
    fun `vue directives mustache ts script and scoped style keep boundary roles`() {
        val code = """
            <template>
              <li v-for="entry in items" :key="entry.id" @click="select(entry)">
                {{ entry.title }}
              </li>
            </template>
            <script setup lang="ts">
            type Entry = { title: string };
            const items: Entry[] = [];
            </script>
            <style scoped>
            .entry { color: red; }
            </style>
        """.trimIndent()

        assertTokenAt("vue", code, "template", "Tag")
        assertTokenAt("vue", code, "v-for", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, ":key", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "@click", "Attribute", "attribute.directive")
        assertTokenAt("vue", code, "{{", "Markup", "markup.expression")
        assertTokenAt("vue", code, "entry", "Variable", occurrence = 3)
        assertTokenAt("vue", code, "title", "Property")
        assertTokenAt("vue", code, "script", "Tag")
        assertTokenAt("vue", code, "setup", "Attribute")
        assertTokenAt("vue", code, "\"ts\"", "String")
        assertTokenAt("vue", code, "type", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "Entry", "Type")
        assertTokenAt("vue", code, "style", "Tag")
        assertTokenAt("vue", code, "scoped", "Attribute")
        assertTokenAt("vue", code, "color", "Property")
    }

    @Test
    fun `vue typed untyped raw text and comments keep boundaries`() {
        val code = """
            <template>
              <p>const outside = 1;</p>
              <!-- :class should stay comment text -->
            </template>
            <script>const plain = 1;</script>
            <script setup lang="ts">interface Props { title: string }</script>
            <style>.plain { color: red; }</style>
            <style lang="less">@brand: red; .typed { color: @brand; }</style>
        """.trimIndent()

        assertTokenAt("vue", code, "template", "Tag")
        assertTokenAt("vue", code, "plain", "Variable")
        assertTokenAt("vue", code, "\"ts\"", "String")
        assertTokenAt("vue", code, "interface", "Keyword", "keyword.declaration")
        assertTokenAt("vue", code, "Props", "Type")
        assertTokenAt("vue", code, ".plain", "Attribute")
        assertTokenAt("vue", code, "color", "Property")
        assertTokenAt("vue", code, "\"less\"", "String")
        assertTokenAt("vue", code, "@brand", "Variable")
        assertTokenAt("vue", code, "@brand", "Variable", occurrence = 1)
        assertNoTokenAt("vue", code, "outside", "Variable")
        assertNoTokenAt("vue", code, ":class", "Attribute")
    }
}
