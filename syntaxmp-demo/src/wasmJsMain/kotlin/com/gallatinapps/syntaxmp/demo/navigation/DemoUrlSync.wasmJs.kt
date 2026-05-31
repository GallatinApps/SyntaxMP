package com.gallatinapps.syntaxmp.demo.navigation

import kotlinx.browser.window
import org.w3c.dom.events.Event

internal actual fun initialDemoRouteFromUrl(): DemoRoute? =
    parseDemoHashRoute(window.location.hash) ?: cleanPathRoute()

internal actual fun writeDemoRouteToUrl(route: DemoRoute?) {
    val targetHash = route?.toHashRoute().orEmpty()
    if (isCleanPathRoute()) {
        window.location.replace("/$targetHash")
        return
    }
    if (window.location.hash != targetHash) {
        window.location.hash = targetHash
    }
}

internal actual fun observeDemoUrlRoutes(onRouteChanged: (DemoRoute?) -> Unit): () -> Unit {
    val listener: (Event) -> Unit = {
        onRouteChanged(initialDemoRouteFromUrl())
    }
    window.addEventListener("hashchange", listener)
    return {
        window.removeEventListener("hashchange", listener)
    }
}

private fun cleanPathRoute(): DemoRoute? {
    val path = cleanPathWithSearch() ?: return null
    return parseDemoPathRoute(path)
}

private fun isCleanPathRoute(): Boolean =
    cleanPathWithSearch() != null

private fun cleanPathWithSearch(): String? {
    val path = window.location.pathname
    return if (path.isBlank() || path == "/" || path == "/index.html" || path == "/404.html") {
        null
    } else {
        path + window.location.search
    }
}
