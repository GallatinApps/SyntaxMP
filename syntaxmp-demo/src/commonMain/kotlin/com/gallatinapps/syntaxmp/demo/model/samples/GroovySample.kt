package com.gallatinapps.syntaxmp.demo.model.samples

internal val GroovySample = """
    plugins {
        id 'com.android.library' version '9.0.1'
        id 'org.jetbrains.kotlin.multiplatform' version '2.3.21'
        id 'org.jetbrains.compose' version '1.11.0'
        id 'org.jetbrains.kotlin.plugin.compose' version '2.3.21'
    }

    group = 'com.gallatinapps.syntaxmp'
    version = '0.1.0-SNAPSHOT'

    ext {
        demoRoute = { language -> "/#/lang/${'$'}language" }
        showcasedTargets = ['jvm', 'android', 'wasmJs']
    }

    repositories {
        mavenCentral()
        google()
    }

    kotlin {
        jvm()
        androidTarget()
        wasmJs {
            browser()
            binaries.executable()
        }
        iosArm64()
        iosSimulatorArm64()

        sourceSets {
            commonMain {
                dependencies {
                    implementation compose.runtime
                    implementation compose.foundation
                    implementation compose.ui
                    implementation project(':syntaxmp')
                }
            }
            commonTest {
                dependencies {
                    implementation kotlin('test')
                }
            }
        }
    }

    android {
        namespace 'com.gallatinapps.syntaxmp.demo'
        compileSdk 36

        defaultConfig {
            minSdk 26
        }
    }

    tasks.register('verifyDemo') {
        group = 'verification'
        description = "Builds the Wasm gallery at ${'$'}{demoRoute('kotlin')}"
        dependsOn ':syntaxmp:jvmTest', ':syntaxmp-demo:compileKotlinWasmJs'

        doLast {
            def joinedTargets = showcasedTargets.collect { it.toUpperCase() }.join(', ')
            def route = demoRoute('groovy')
            assert route ==~ /\/#\/lang\/\w+/
            logger.lifecycle("Verified ${'$'}joinedTargets for ${'$'}route")
        }
    }

    configurations.configureEach { config ->
        resolutionStrategy.eachDependency { details ->
            if (details.requested.group == 'org.jetbrains.kotlin') {
                details.useVersion '2.3.21'
            }
        }
    }
""".trimIndent()
