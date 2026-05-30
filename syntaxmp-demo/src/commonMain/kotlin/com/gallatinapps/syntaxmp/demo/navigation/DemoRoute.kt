package com.gallatinapps.syntaxmp.demo.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
internal sealed interface DemoRoute : NavKey {
    @Serializable
    data object LanguageIndex : DemoRoute

    @Serializable
    data object GetStarted : DemoRoute

    @Serializable
    data class LanguagePreview(
        val routeSegment: String,
    ) : DemoRoute

    @Serializable
    data class NotFound(
        val originalPath: String,
    ) : DemoRoute
}

internal val demoRouteSerializer = PolymorphicSerializer(NavKey::class)

internal val demoRouteSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(DemoRoute.LanguageIndex::class, DemoRoute.LanguageIndex.serializer())
        subclass(DemoRoute.GetStarted::class, DemoRoute.GetStarted.serializer())
        subclass(DemoRoute.LanguagePreview::class, DemoRoute.LanguagePreview.serializer())
        subclass(DemoRoute.NotFound::class, DemoRoute.NotFound.serializer())
    }
}

internal val demoRouteSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = demoRouteSerializersModule
}
