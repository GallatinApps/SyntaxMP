package com.gallatinapps.syntaxmp.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.gallatinapps.syntaxmp.demo.model.DemoLanguage
import com.gallatinapps.syntaxmp.demo.model.DemoLanguageCatalog
import com.gallatinapps.syntaxmp.demo.navigation.DemoRoute
import com.gallatinapps.syntaxmp.demo.navigation.demoRouteSavedStateConfiguration
import com.gallatinapps.syntaxmp.demo.navigation.initialDemoRouteFromUrl
import com.gallatinapps.syntaxmp.demo.navigation.lastDetailRoute
import com.gallatinapps.syntaxmp.demo.navigation.navigateBack
import com.gallatinapps.syntaxmp.demo.navigation.navigateToGetStarted
import com.gallatinapps.syntaxmp.demo.navigation.navigateToLanguage
import com.gallatinapps.syntaxmp.demo.navigation.observeDemoUrlRoutes
import com.gallatinapps.syntaxmp.demo.navigation.replaceMissingLanguageRoute
import com.gallatinapps.syntaxmp.demo.navigation.replaceDetailRoute
import com.gallatinapps.syntaxmp.demo.navigation.selectedDetailRouteOrNull
import com.gallatinapps.syntaxmp.demo.navigation.selectedLanguageOrNull
import com.gallatinapps.syntaxmp.demo.navigation.writeDemoRouteToUrl
import com.gallatinapps.syntaxmp.demo.panes.GetStartedPane
import com.gallatinapps.syntaxmp.demo.panes.LanguageListPane
import com.gallatinapps.syntaxmp.demo.panes.NotFoundPane
import com.gallatinapps.syntaxmp.demo.panes.SyntaxPreviewPane
import com.gallatinapps.syntaxmp.demo.theme.DemoColorScheme
import com.gallatinapps.syntaxmp.demo.theme.DemoThemeMode
import com.gallatinapps.syntaxmp.demo.theme.colors
import com.gallatinapps.syntaxmp.demo.theme.updateBrowserShellTheme

private val WideLayoutMinWidth = 900.dp

@Composable
internal fun SyntaxDemoApp() {
    val builtInLanguages = remember {
        DemoLanguageCatalog.Languages.sortedBy { language -> language.displayName.lowercase() }
    }
    val defaultLanguage = requireNotNull(DemoLanguageCatalog.defaultLanguage())
    val initialRoute = remember { initialDemoRouteFromUrl() }
    val initialBackStack = remember(initialRoute) {
        buildList<NavKey> {
            add(DemoRoute.LanguageIndex)
            if (initialRoute != null && initialRoute !is DemoRoute.LanguageIndex) {
                add(initialRoute)
            }
        }.toTypedArray()
    }
    val backStack: MutableList<NavKey> = rememberNavBackStack(
        demoRouteSavedStateConfiguration,
        *initialBackStack,
    )
    var themeMode by remember { mutableStateOf(DemoThemeMode.Dark) }
    val colors = themeMode.colors
    val detailRoute = backStack.lastDetailRoute()
    val selectedDetailRoute = backStack.selectedDetailRouteOrNull()
    val selectedLanguage = backStack.selectedLanguageOrNull()

    LaunchedEffect(themeMode) {
        updateBrowserShellTheme(themeMode)
    }

    LaunchedEffect(
        (detailRoute as? DemoRoute.LanguagePreview)?.routeSegment,
        defaultLanguage.routeSegment,
    ) {
        backStack.replaceMissingLanguageRoute(defaultLanguage)
    }

    DisposableEffect(backStack) {
        val dispose = observeDemoUrlRoutes { route ->
            backStack.replaceDetailRoute(route)
        }
        onDispose {
            dispose()
        }
    }

    val urlRoute = (backStack.lastOrNull() as? DemoRoute)
        ?.takeUnless { route -> route is DemoRoute.LanguageIndex }
    LaunchedEffect(urlRoute) {
        writeDemoRouteToUrl(urlRoute)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.appBackground),
    ) {
        if (maxWidth >= WideLayoutMinWidth) {
            WideDemoLayout(
                languages = builtInLanguages,
                detailRoute = detailRoute,
                selectedDetailRoute = selectedDetailRoute,
                selectedLanguage = selectedLanguage ?: defaultLanguage,
                themeMode = themeMode,
                onThemeModeChanged = { themeMode = it },
                onGetStartedSelected = {
                    backStack.navigateToGetStarted()
                },
                onLanguageSelected = { language ->
                    backStack.navigateToLanguage(language.routeSegment)
                },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CompactDemoLayout(
                languages = builtInLanguages,
                defaultLanguage = defaultLanguage,
                backStack = backStack,
                themeMode = themeMode,
                onThemeModeChanged = { themeMode = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun WideDemoLayout(
    languages: List<DemoLanguage>,
    detailRoute: DemoRoute,
    selectedDetailRoute: DemoRoute?,
    selectedLanguage: DemoLanguage,
    themeMode: DemoThemeMode,
    onThemeModeChanged: (DemoThemeMode) -> Unit,
    onGetStartedSelected: () -> Unit,
    onLanguageSelected: (DemoLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = themeMode.colors
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DemoPaneFrame(
            colors = colors,
            modifier = Modifier
                .width(292.dp)
                .fillMaxHeight(),
        ) {
            LanguageListPane(
                languages = languages,
                getStartedSelected = detailRoute is DemoRoute.GetStarted,
                selectedRouteSegment = (selectedDetailRoute as? DemoRoute.LanguagePreview)?.routeSegment,
                colors = colors,
                onGetStartedSelected = onGetStartedSelected,
                onLanguageSelected = onLanguageSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }
        DemoPaneFrame(
            colors = colors,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            when (detailRoute) {
                DemoRoute.GetStarted,
                DemoRoute.LanguageIndex -> GetStartedPane(
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    modifier = Modifier.fillMaxSize(),
                )
                is DemoRoute.LanguagePreview -> SyntaxPreviewPane(
                    language = selectedLanguage,
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    modifier = Modifier.fillMaxSize(),
                )
                is DemoRoute.NotFound -> NotFoundPane(
                    originalPath = detailRoute.originalPath,
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    onGetStarted = onGetStartedSelected,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun CompactDemoLayout(
    languages: List<DemoLanguage>,
    defaultLanguage: DemoLanguage,
    backStack: MutableList<NavKey>,
    themeMode: DemoThemeMode,
    onThemeModeChanged: (DemoThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = themeMode.colors
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = {
            backStack.navigateBack()
        },
        entryProvider = entryProvider {
            entry<DemoRoute.LanguageIndex> {
                val selectedDetailRoute = backStack.selectedDetailRouteOrNull()
                LanguageListPane(
                    languages = languages,
                    getStartedSelected = selectedDetailRoute is DemoRoute.GetStarted,
                    selectedRouteSegment = (selectedDetailRoute as? DemoRoute.LanguagePreview)?.routeSegment,
                    colors = colors,
                    onGetStartedSelected = {
                        backStack.navigateToGetStarted()
                    },
                    onLanguageSelected = { language ->
                        backStack.navigateToLanguage(language.routeSegment)
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<DemoRoute.GetStarted> {
                GetStartedPane(
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    onBack = { backStack.navigateBack() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<DemoRoute.LanguagePreview> { route ->
                val language = DemoLanguageCatalog.languageByRouteSegment(route.routeSegment) ?: defaultLanguage
                SyntaxPreviewPane(
                    language = language,
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    onBack = { backStack.navigateBack() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<DemoRoute.NotFound> { route ->
                NotFoundPane(
                    originalPath = route.originalPath,
                    mode = themeMode,
                    colors = colors,
                    onModeChanged = onThemeModeChanged,
                    onGetStarted = { backStack.navigateToGetStarted() },
                    onBack = { backStack.navigateBack() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}

@Composable
private fun DemoPaneFrame(
    colors: DemoColorScheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.paneBackground)
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(8.dp)),
    ) {
        content()
    }
}
