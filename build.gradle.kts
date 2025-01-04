import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    embeddedKotlin("jvm") apply false
    alias(libs.plugins.indra) apply false
    alias(libs.plugins.indra.licenserSpotless) apply false
    alias(libs.plugins.indra.gradlePlugin) apply false
}

group = "ca.stellardrift"
version = "6.2.0-SNAPSHOT"
description = "A suite of plugins to apply defaults preferred for Stellardrift projects"

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="net.kyori.indra")
    apply(plugin="net.kyori.indra.licenser.spotless")
    apply(plugin="net.kyori.indra.publishing.gradle-plugin")
    apply(plugin="org.jetbrains.kotlin.jvm")

    configurations.configureEach {
        if (isCanBeConsumed) {
            val usage = attributes.getAttribute(Usage.USAGE_ATTRIBUTE) ?: return@configureEach
            if (usage.name in setOf(Usage.JAVA_RUNTIME, Usage.JAVA_API)) {
                attributes.attribute(
                    GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                    objects.named(GradleVersion.current().version)
                )
            }
        }
    }

    extensions.configure(KotlinJvmProjectExtension::class) {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.addAll(listOf("-Xjvm-default=all", "-Xjdk-release=17"))
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
        javaVersions().target(17)

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

