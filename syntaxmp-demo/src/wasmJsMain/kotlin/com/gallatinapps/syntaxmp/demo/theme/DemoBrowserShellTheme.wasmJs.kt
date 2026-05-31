package com.gallatinapps.syntaxmp.demo.theme

import kotlinx.browser.document
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.HTMLElement

internal actual fun updateBrowserShellTheme(mode: DemoThemeMode) {
    val background = when (mode) {
        DemoThemeMode.Light -> "#f4f4f5"
        DemoThemeMode.Dark -> "#141414"
        DemoThemeMode.Diagnostic -> "#0a0a0a"
    }
    val colorScheme = when (mode) {
        DemoThemeMode.Light -> "light"
        DemoThemeMode.Dark,
        DemoThemeMode.Diagnostic -> "dark"
    }

    val themeMeta = document.querySelector("meta[name='theme-color']") as? HTMLMetaElement
    themeMeta?.content = background

    (document.documentElement as? HTMLElement)?.style?.apply {
        backgroundColor = background
        setProperty("color-scheme", colorScheme)
    }
    document.body?.style?.backgroundColor = background
}
