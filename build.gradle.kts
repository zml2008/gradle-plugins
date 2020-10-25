import com.gradle.publish.PluginBundleExtension

plugins {
    kotlin("jvm") version "1.3.72" apply false // we must override what we're providing ourself... whoo circular dependencies

    id("ca.stellardrift.opinionated.kotlin") version "3.1" apply false
    id("com.github.ben-manes.versions") version "0.28.0"
}

group = "ca.stellardrift"
version = "4.0-SNAPSHOT"

subprojects {
    apply(plugin="java-gradle-plugin")
    apply(plugin="com.gradle.plugin-publish")
    apply(plugin="maven-publish")
    apply(plugin="ca.stellardrift.opinionated.kotlin")

    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        "implementation"(gradleKotlinDsl())

        "testImplementation"(kotlin("test"))
        "testImplementation"(kotlin("test-junit"))
    }

    repositories {
        jcenter()
        gradlePluginPortal()
    }

    val pluginBundle = extensions.getByType(PluginBundleExtension::class).apply {
        website = "https://github.com/zml2008/gradle-plugins"
        vcsUrl = "https://github.com/zml2008/gradle-plugins.git"
        description = "A suite of plugins to apply defaults preferred for Stellardrift projects"
        tags = listOf("minecraft", "opinionated", "defaults")
    }

    extensions.getByType(PublishingExtension::class).publications.withType(MavenPublication::class).configureEach {
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

