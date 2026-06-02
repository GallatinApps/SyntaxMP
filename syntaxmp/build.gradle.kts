import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechMavenPublish)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    explicitApi()

    android {
        namespace = "com.gallatinapps.syntaxmp.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    wasmJs {
        browser()
    }
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            api(project(":syntaxmp-tokenizer"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies {
            implementation(libs.compose.uiTestJunit4)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("com.gallatinapps.syntaxmp", "syntaxmp", project.version.toString())

    pom {
        name.set("SyntaxMP")
        description.set("Kotlin Multiplatform syntax highlighting for Compose.")
        url.set("https://demo.syntaxmp.com")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("gallatinapps")
                name.set("Gallatin Applications LLC")
                email.set("support@gallatinapps.com")
                organization.set("Gallatin Applications LLC")
                organizationUrl.set("https://gallatinapps.com")
            }
        }

        scm {
            connection.set("scm:git:https://github.com/GallatinApps/SyntaxMP.git")
            developerConnection.set("scm:git:ssh://git@github.com/GallatinApps/SyntaxMP.git")
            url.set("https://github.com/GallatinApps/SyntaxMP")
        }
    }
}
