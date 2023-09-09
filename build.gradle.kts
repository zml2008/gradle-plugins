import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.indra) apply false
    alias(libs.plugins.indra.licenserSpotless) apply false
    alias(libs.plugins.indra.gradlePlugin) apply false
}

group = "ca.stellardrift"
version = "6.1.1-SNAPSHOT"
description = "A suite of plugins to apply defaults preferred for Stellardrift projects"

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="net.kyori.indra")
    apply(plugin="net.kyori.indra.licenser.spotless")
    apply(plugin="net.kyori.indra.publishing.gradle-plugin")
    apply(plugin="org.jetbrains.kotlin.jvm")

    extensions.configure(KotlinJvmProjectExtension::class) {
        coreLibrariesVersion = "1.5.31"
        target {
            compilations.configureEach {
                kotlinOptions {
                    languageVersion = "1.4"
                    freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=all")
                }
            }
        }
    }

    dependencies {
        "implementation"(gradleKotlinDsl())

        "testImplementation"(kotlin("test"))
        "testImplementation"(kotlin("test-junit5"))
        "testImplementation"(platform(rootProject.libs.junit.bom))
        "testImplementation"(rootProject.libs.junit.api)
        "testRuntimeOnly"(rootProject.libs.junit.engine)
        "testRuntimeOnly"(rootProject.libs.junit.launcher)
        "implementation"(rootProject.libs.mammoth)
    }

    extensions.configure(net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension::class) {
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    }

    extensions.configure(SpotlessExtension::class) {
        ratchetFrom("origin/trunk")
        fun com.diffplug.gradle.spotless.FormatExtension.commonOptions() {
            endWithNewline()
            trimTrailingWhitespace()
            indentWithSpaces(4)
        }
        java {
            commonOptions()
            removeUnusedImports()
            importOrder("", "\\#")
        }

        val ktlintOptions = mapOf("ij_kotlin_imports_layout" to "*")
        kotlin {
            commonOptions()
            ktlint(libs.versions.ktlint.get())
                .editorConfigOverride(ktlintOptions)
        }

        kotlinGradle {
            commonOptions()
            ktlint(libs.versions.ktlint.get())
                .editorConfigOverride(ktlintOptions)
        }
    }

    extensions.configure(net.kyori.indra.IndraExtension::class) {
        github("zml2008", "gradle-plugins") {
            ci(true)
        }
        apache2License()

        configurePublications {
            pom {
                developers {
                    developer {
                        name.set("zml")
                        email.set("zml at stellardrift [.] ca")
                    }
                }
            }
        }

        publishReleasesTo("stellardrift", "https://repo.stellardrift.ca/repository/releases/")
        publishSnapshotsTo("stellardrift", "https://repo.stellardrift.ca/repository/snapshots/")
    }

    extensions.configure(net.kyori.indra.gradle.IndraPluginPublishingExtension::class) {
        website("https://github.com/zml2008/gradle-plugins")
        bundleTags("minecraft", "opinionated", "defaults")
    }
}

