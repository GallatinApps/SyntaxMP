import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.vanniktechMavenPublish)
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    explicitApi()

    android {
        namespace = "com.gallatinapps.syntaxmp"
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
    js {
        browser()
    }
    wasmJs {
        browser()
    }
    jvmToolchain(17)

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("com.gallatinapps.syntaxmp", "syntaxmp-tokenizer", project.version.toString())

    pom {
        name.set("SyntaxMP Tokenizer")
        description.set("Kotlin Multiplatform syntax tokenization for SyntaxMP.")
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
