import ca.stellardrift.build.self.declarePlugin

dependencies {
    api("net.kyori:indra-common:1.2.0")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
    api("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
}

declarePlugin(
    id = "opinionated",
    mainClass = "common.OpinionatedDefaultsPlugin",
    displayName = "Opinionated JVM Defaults",
    description = "Some basic configuration for JVM projects"
)
