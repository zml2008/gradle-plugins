import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
    `maven-publish`

    kotlin("jvm") version "1.3.71"
    id("net.minecrell.licenser") version "0.4.1"

}

group = "ca.stellardrift"
version = "1.1-SNAPSHOT"

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Plugin dependencies
    implementation(kotlin("gradle-plugin"))
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.0.2")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}


val javaRev = JavaVersion.VERSION_1_8
java {
    sourceCompatibility = javaRev
    targetCompatibility = javaRev
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = javaRev.toString()
}

license {
    header = rootProject.file("LICENSE_HEADER")
}


gradlePlugin {
    val idBase = "ca.stellardrift"
    plugins {
        fun plugin(id: String, mainClass: String, displayName: String, description: String? = null, tags: List<String> = listOf()) = create(id) {
            val qualifiedId = "$idBase.$id"
            this.id = qualifiedId
            implementationClass = "$idBase.build.$mainClass"
            pluginBundle.plugins.maybeCreate(id).apply {
                this.id = qualifiedId
                this.displayName = displayName
                if (tags.isNotEmpty()) {
                    this.tags = tags
                }
                if (description != null) {
                    this.description = description
                }
            }
        }

        plugin(
            id = "opinionated",
            mainClass = "OpinionatedDefaultsPlugin",
            displayName = "Opinionated JVM Defaults",
            description = "Some basic configuration for JVM projects"
        )
        plugin(
            "opinionated.kotlin",
            "OpinionatedKotlinDefaultsPlugin",
            "Opinionated Kotlin Defaults",
            "Some basic configuration for Kotlin projects"
        )
        plugin(
                id = "opinionated.publish",
                mainClass = "OpinionatedPublishingPlugin",
                displayName = "Opinionated Publishing",
                description = "Common publishing setup options"
        )
        plugin(
            "localization",
            "LocalizationPlugin",
            "Localization",
            "Code generation for resource bundle strings",
                tags = listOf("codegen", "i18n", "l10n", "generation")
        )
        plugin(
            "templating",
            "TemplatingPlugin",
            "Templating",
            "Code templates",
            tags = listOf("codegen", "templates", "generation")
        )
    }
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

