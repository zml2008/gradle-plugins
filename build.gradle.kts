import ca.stellardrift.build.self.declarePlugin

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    `maven-publish`

    id("ca.stellardrift.opinionated.kotlin") version "2.0"

}

group = "ca.stellardrift"
version = "2.1-SNAPSHOT"


dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Plugin dependencies
    implementation(kotlin("gradle-plugin"))
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.0.2")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

    declarePlugin(
            id = "opinionated",
            mainClass = "OpinionatedDefaultsPlugin",
            displayName = "Opinionated JVM Defaults",
            description = "Some basic configuration for JVM projects"
    )
    declarePlugin(
            "opinionated.kotlin",
            "OpinionatedKotlinDefaultsPlugin",
            "Opinionated Kotlin Defaults",
            "Some basic configuration for Kotlin projects"
    )
    declarePlugin(
            id = "opinionated.publish",
            mainClass = "OpinionatedPublishingPlugin",
            displayName = "Opinionated Publishing",
            description = "Common publishing setup options"
    )
    declarePlugin(
            "localization",
            "LocalizationPlugin",
            "Localization",
            "Code generation for resource bundle strings",
            tags = listOf("codegen", "i18n", "l10n", "generation")
    )
    declarePlugin(
            "templating",
            "TemplatingPlugin",
            "Templating",
            "Code templates",
            tags = listOf("codegen", "templates", "generation")
    )

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="maven-publish")
    apply(plugin="ca.stellardrift.opinionated.kotlin")
}

allprojects {
    repositories {
        jcenter()
        gradlePluginPortal()
    }

    license {
        header = rootProject.file("LICENSE_HEADER")
    }

    pluginBundle {
        website = "https://github.com/zml2008/gradle-plugins"
        vcsUrl = "https://github.com/zml2008/gradle-plugins"
        description = "A suite of plugins to apply defaults preferred for Stellardrift projects"
        tags = listOf("minecraft", "opinionated", "defaults")
    }

    publishing.publications.withType(MavenPublication::class).configureEach {
        pom {
            name.set(project.name)
            description.set(pluginBundle.description)
            url.set(pluginBundle.vcsUrl)

            developers {
                developer {
                    name.set("zml")
                    email.set("zml at stellardrift [.] ca")
                }
            }

            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
        }
    }
}

