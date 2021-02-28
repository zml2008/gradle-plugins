import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm") version "1.3.72" apply false // we must override what we're providing ourself... whoo circular dependencies

    id("net.kyori.indra.publishing.gradle-plugin") version "1.3.1" apply false
    id("ca.stellardrift.opinionated.kotlin") version "4.1" apply false
    id("com.github.ben-manes.versions") version "0.36.0"
}

group = "ca.stellardrift"
version = "4.2-SNAPSHOT"
description = "A suite of plugins to apply defaults preferred for Stellardrift projects"

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="net.kyori.indra.publishing.gradle-plugin")
    apply(plugin="ca.stellardrift.opinionated.kotlin")

    dependencies {
        val junitVersion: String by project
        "implementation"(gradleKotlinDsl())

        "testImplementation"(kotlin("test", embeddedKotlinVersion))
        "testImplementation"(kotlin("test-junit5", embeddedKotlinVersion))
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
        kotlinOptions.languageVersion = "1.3"
    }

    extensions.configure(net.kyori.indra.IndraExtension::class) {
        github("zml2008", "gradle-plugins") {
            ci = true
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
    }

    val pluginBundle = extensions.getByType(PluginBundleExtension::class).apply {
        website = "https://github.com/zml2008/gradle-plugins"
        tags = listOf("minecraft", "opinionated", "defaults")
    }
}

