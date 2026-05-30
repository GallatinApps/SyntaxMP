import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
}

allprojects {
    group = "com.gallatinapps.syntaxmp"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories.maven {
                name = "buildRepo"
                url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
            }
            publications.withType(MavenPublication::class.java).configureEach {
                pom {
                    name.set(
                        when (project.name) {
                            "syntaxmp" -> "SyntaxMP"
                            else -> project.name
                        },
                    )
                    description.set(
                        when (project.name) {
                            "syntaxmp" -> "Kotlin Multiplatform syntax highlighting models, tokenizers, and Compose text helpers."
                            else -> "SyntaxMP module ${project.name}."
                        },
                    )
                    url.set("https://demo.syntaxmp.com")
                    developers {
                        developer {
                            id.set("gallatinapps")
                            name.set("Gallatin Applications LLC")
                            organization.set("Gallatin Applications LLC")
                            organizationUrl.set("https://demo.syntaxmp.com")
                        }
                    }
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/GallatinApps/SyntaxMP.git")
                        developerConnection.set("scm:git:ssh://git@github.com/GallatinApps/SyntaxMP.git")
                        url.set("https://github.com/GallatinApps/SyntaxMP")
                    }
                }
            }
        }
    }
}
