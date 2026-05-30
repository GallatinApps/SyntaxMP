package com.gallatinapps.syntaxmp.demo.navigation

internal expect fun initialDemoRouteFromUrl(): DemoRoute?

internal expect fun writeDemoRouteToUrl(route: DemoRoute?)

internal expect fun observeDemoUrlRoutes(onRouteChanged: (DemoRoute?) -> Unit): () -> Unit
