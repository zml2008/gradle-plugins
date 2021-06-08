import com.gradle.publish.PluginBundleExtension
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    kotlin("jvm") version "1.4.10" apply false // we must override what we're providing ourself... whoo circular dependencies

    val indraVersion = "2.0.4"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.license-header") version indraVersion apply false
    id("net.kyori.indra.publishing.gradle-plugin") version indraVersion apply false
    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "ca.stellardrift"
version = "5.0.1-SNAPSHOT"
description = "A suite of plugins to apply defaults preferred for Stellardrift projects"

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="net.kyori.indra")
    apply(plugin="net.kyori.indra.license-header")
    apply(plugin="net.kyori.indra.publishing.gradle-plugin")
    apply(plugin="org.jetbrains.kotlin.jvm")

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
        kotlinOptions {
            languageVersion = "1.3"
            freeCompilerArgs = freeCompilerArgs + listOf("-Xjvm-default=enable")
            jvmTarget = "1.8"
        }
    }

    dependencies {
        val junitVersion: String by project
        "implementation"(gradleKotlinDsl())

        "testImplementation"(kotlin("test", embeddedKotlinVersion))
        "testImplementation"(kotlin("test-junit5", embeddedKotlinVersion))
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }

    repositories {
        maven("https://repo.stellardrift.ca/repository/internal/") {
            name = "stellardriftReleases"
            mavenContent { releasesOnly() }
        }
        maven("https://repo.stellardrift.ca/repository/snapshots/") {
            name = "stellardriftSnapshots"
            mavenContent { snapshotsOnly() }
        }
    }

    extensions.configure(LicenseExtension::class) {
        header(rootProject.file("LICENSE_HEADER"))
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

    val pluginBundle = extensions.getByType(PluginBundleExtension::class).apply {
        website = "https://github.com/zml2008/gradle-plugins"
        tags = listOf("minecraft", "opinionated", "defaults")
    }
}

