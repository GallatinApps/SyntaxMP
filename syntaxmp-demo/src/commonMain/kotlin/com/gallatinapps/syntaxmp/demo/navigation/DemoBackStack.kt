package com.gallatinapps.syntaxmp.demo.navigation

import androidx.navigation3.runtime.NavKey
import com.gallatinapps.syntaxmp.demo.model.DemoLanguage
import com.gallatinapps.syntaxmp.demo.model.DemoLanguageCatalog

internal fun List<NavKey>.lastDetailRoute(): DemoRoute =
    lastOrNull { route ->
        route.isDetailRoute
    } as? DemoRoute ?: DemoRoute.GetStarted

internal fun List<NavKey>.selectedDetailRouteOrNull(): DemoRoute? =
    lastOrNull { route ->
        route.isDetailRoute
    } as? DemoRoute

internal fun List<NavKey>.selectedLanguageOrNull(): DemoLanguage? =
    (selectedDetailRouteOrNull() as? DemoRoute.LanguagePreview)?.let { route ->
        DemoLanguageCatalog.languageByRouteSegment(route.routeSegment)
    }

internal fun MutableList<NavKey>.replaceMissingLanguageRoute(defaultLanguage: DemoLanguage) {
    val index = indexOfLast { it is DemoRoute.LanguagePreview }
    if (index == -1) {
        return
    }
    val route = this[index] as DemoRoute.LanguagePreview
    if (DemoLanguageCatalog.languageByRouteSegment(route.routeSegment) == null) {
        this[index] = DemoRoute.LanguagePreview(defaultLanguage.routeSegment)
    }
}

internal fun MutableList<NavKey>.navigateToGetStarted() {
    navigateToDetail(DemoRoute.GetStarted)
}

internal fun MutableList<NavKey>.navigateToLanguage(routeSegment: String) {
    navigateToDetail(DemoRoute.LanguagePreview(routeSegment))
}

internal fun MutableList<NavKey>.navigateToNotFound(originalPath: String) {
    navigateToDetail(DemoRoute.NotFound(originalPath))
}

internal fun MutableList<NavKey>.replaceDetailRoute(route: DemoRoute?) {
    val detailIndex = indexOfLast { key -> key.isDetailRoute }
    if (route == null || route is DemoRoute.LanguageIndex) {
        if (detailIndex != -1) {
            removeAt(detailIndex)
        }
        return
    }
    if (detailIndex == -1) {
        add(route)
    } else if (this[detailIndex] != route) {
        this[detailIndex] = route
    }
}

private fun MutableList<NavKey>.navigateToDetail(route: DemoRoute) {
    if (lastOrNull() == route) {
        return
    }
    replaceDetailRoute(route)
}

internal fun MutableList<NavKey>.navigateBack() {
    if (size > 1) {
        removeAt(lastIndex)
    }
}

private val NavKey.isDetailRoute: Boolean
    get() = this is DemoRoute.GetStarted ||
        this is DemoRoute.LanguagePreview ||
        this is DemoRoute.NotFound
