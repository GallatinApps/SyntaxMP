package com.gallatinapps.syntaxmp.engine.scanners.markup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MarkupScannerOptionsTest {
    @Test
    fun `default resolves script bodies`() {
        assertEquals("javascript", resolveDefault("script"))
        assertEquals("ts", resolveDefault("script", "lang" to "ts"))
        assertEquals("typescript", resolveDefault("script", "lang" to "typescript"))
        assertEquals("tsx", resolveDefault("script", "lang" to "tsx"))
        assertEquals("jsx", resolveDefault("script", "lang" to "jsx"))
        assertEquals("coffee", resolveDefault("script", "lang" to "coffee"))
    }

    @Test
    fun `default resolves style bodies`() {
        assertEquals("css", resolveDefault("style"))
        assertEquals("scss", resolveDefault("style", "lang" to "scss"))
        assertEquals("sass", resolveDefault("style", "lang" to "sass"))
        assertEquals("less", resolveDefault("style", "lang" to "less"))
        assertEquals("postcss", resolveDefault("style", "lang" to "postcss"))
    }

    @Test
    fun `default ignores unknown tags`() {
        assertNull(resolveDefault("template"))
    }

    @Test
    fun `no raw text language option never resolves a language`() {
        assertNull(MarkupScannerOptions.noRawTextLanguageForTag("script", "ts"))
        assertNull(MarkupScannerOptions.noRawTextLanguageForTag("style", "scss"))
    }

    private fun resolveDefault(
        tagName: String,
        vararg attributes: Pair<String, String>,
    ) = MarkupScannerOptions.defaultRawTextLanguageForTag(
        tagName = tagName,
        lang = attributes.firstOrNull { it.first == "lang" }?.second,
    )
}
