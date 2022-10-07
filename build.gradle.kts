import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "1.7.20" apply false // we must override what we're providing ourself... whoo circular dependencies

    val indraVersion = "3.0.0"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.licenser.spotless") version indraVersion apply false
    id("net.kyori.indra.publishing.gradle-plugin") version indraVersion apply false
}

group = "ca.stellardrift"
version = "6.0.0-SNAPSHOT"
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
                    languageVersion = "1.3"
                    freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=all")
                }
            }
        }
    }

    dependencies {
        val junitVersion: String by project
        "implementation"(gradleKotlinDsl())

        "testImplementation"(kotlin("test"))
        "testImplementation"(kotlin("test-junit5"))
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        "implementation"("net.kyori:mammoth:1.3.0-SNAPSHOT")
    }

    extensions.configure(net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension::class) {
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
    }

    extensions.configure(SpotlessExtension::class) {
        ratchetFrom("origin/dev")
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

