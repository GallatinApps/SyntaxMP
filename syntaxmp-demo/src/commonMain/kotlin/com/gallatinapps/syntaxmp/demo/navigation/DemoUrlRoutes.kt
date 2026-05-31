package com.gallatinapps.syntaxmp.demo.navigation

import com.gallatinapps.syntaxmp.demo.model.DemoLanguageCatalog

private const val GetStartedPath = "/getting-started"
private const val GetStartedPathCompact = "/gettingstarted"
private const val LanguagePathPrefix = "/lang/"
private const val NotFoundPath = "/404"
private const val NotFoundPathPrefix = "/404/"

internal fun parseDemoHashRoute(rawHash: String): DemoRoute? {
    val path = rawHash
        .removePrefix("#")
        .normalizedHashPath()
    return parseDemoPath(path)
}

internal fun parseDemoPathRoute(rawPath: String): DemoRoute? {
    val path = rawPath.normalizedHashPath()
    return if (path == "/index.html" || path == "/404.html") {
        null
    } else {
        parseDemoPath(path)
    }
}

private fun parseDemoPath(path: String): DemoRoute? {
    if (path == "/") {
        return null
    }

    val comparablePath = path.lowercase()
    return when {
        comparablePath == GetStartedPath || comparablePath == GetStartedPathCompact -> {
            DemoRoute.GetStarted
        }
        comparablePath.startsWith(LanguagePathPrefix) -> {
            parseLanguageRoute(path)
        }
        comparablePath == NotFoundPath -> {
            DemoRoute.NotFound("/")
        }
        comparablePath.startsWith(NotFoundPathPrefix) -> {
            DemoRoute.NotFound(path.removePrefix(NotFoundPath))
        }
        else -> {
            DemoRoute.NotFound(path)
        }
    }
}

internal fun DemoRoute.toHashRoute(): String? =
    when (this) {
        DemoRoute.LanguageIndex -> null
        DemoRoute.GetStarted -> "#$GetStartedPath"
        is DemoRoute.LanguagePreview -> "#$LanguagePathPrefix$routeSegment"
        is DemoRoute.NotFound -> "#$NotFoundPath${originalPath.notFoundHashSuffix()}"
    }

private fun parseLanguageRoute(path: String): DemoRoute {
    val languageTail = path.drop(LanguagePathPrefix.length)
    val languageSegment = languageTail.substringBefore("?")
    if (languageSegment.isBlank() || "/" in languageSegment) {
        return DemoRoute.NotFound(path)
    }
    val language = DemoLanguageCatalog.languageByRouteSegment(languageSegment)
    return if (language == null) {
        DemoRoute.NotFound(path)
    } else {
        DemoRoute.LanguagePreview(language.routeSegment)
    }
}

private fun String.normalizedHashPath(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) {
        return "/"
    }
    return if (trimmed.startsWith("/")) trimmed else "/$trimmed"
}

private fun String.notFoundHashSuffix(): String {
    val trimmed = trim()
    return when {
        trimmed.isBlank() || trimmed == "/" -> ""
        trimmed.startsWith("/") -> trimmed
        else -> "/$trimmed"
    }
}
