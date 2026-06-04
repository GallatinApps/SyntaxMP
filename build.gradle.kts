plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.vanniktechMavenPublish) apply false
}

allprojects {
    group = "com.gallatinapps.syntaxmp"
    version = "0.3.0"
}
