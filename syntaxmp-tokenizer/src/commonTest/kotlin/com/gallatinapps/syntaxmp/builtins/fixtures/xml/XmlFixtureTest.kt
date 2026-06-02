package com.gallatinapps.syntaxmp.builtins.fixtures.xml

import com.gallatinapps.syntaxmp.builtins.fixtures.assertNoTokenAt
import com.gallatinapps.syntaxmp.builtins.fixtures.assertTokenAt
import kotlin.test.Test

class XmlFixtureTest {
    private val code = """
        <?xml version="1.0" encoding="utf-8"?>
        <!-- vector-style sample -->
        <vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="24dp">
          <![CDATA[if (count < 4) keep text as character data]]>
          <path android:fillColor="#38A3FF" android:pathData="M4,4 L20,20" />
        </vector>
    """.trimIndent()

    @Test
    fun `xml processing instructions cdata tags attributes and values`() {
        assertTokenAt("xml", code, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", "Annotation")
        assertTokenAt("xml", code, "<!-- vector-style sample -->", "Comment")
        assertTokenAt("xml", code, "vector", "Tag", occurrence = 1)
        assertTokenAt("xml", code, "xmlns:android", "Attribute")
        assertTokenAt("xml", code, "\"http://schemas.android.com/apk/res/android\"", "String")
        assertTokenAt("xml", code, "android:width", "Attribute")
        assertTokenAt("xml", code, "\"24dp\"", "String")
        assertTokenAt("xml", code, "<![CDATA[", "Markup")
        assertTokenAt("xml", code, "if (count < 4) keep text as character data", "String")
        assertTokenAt("xml", code, "]]>", "Markup")
    }

    @Test
    fun `cdata text is not tokenized as markup`() = assertNoTokenAt(
        language = "xml",
        code = code,
        substring = "count",
        category = "Attribute",
    )

    @Test
    fun `xml entities tokenize in text and attributes but not cdata`() {
        val code = """<text value="Tom &amp; Jerry">A &#169; B <![CDATA[&amp;]]></text>"""

        assertTokenAt("xml", code, "&amp;", "Escape")
        assertTokenAt("xml", code, "&#169;", "Escape")
        assertNoTokenAt("xml", code, "&amp;", "Escape", occurrence = 1)
    }

    @Test
    fun `xml script and style elements do not route to child languages`() {
        val code = """<root><script>let x = 1;</script><style>.card { color: red; }</style></root>"""

        assertTokenAt("xml", code, "root", "Tag")
        assertTokenAt("xml", code, "script", "Tag")
        assertTokenAt("xml", code, "script", "Tag", occurrence = 1)
        assertTokenAt("xml", code, "style", "Tag")
        assertTokenAt("xml", code, "style", "Tag", occurrence = 1)
        assertNoTokenAt("xml", code, "let", "Keyword")
        assertNoTokenAt("xml", code, "x", "Variable")
        assertNoTokenAt("xml", code, ".card", "Attribute")
        assertNoTokenAt("xml", code, "color", "Property")
    }
}
