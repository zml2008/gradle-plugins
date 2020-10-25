import ca.stellardrift.build.self.declarePlugin

dependencies {
    api("net.kyori:indra-common:1.0.1")
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
}

declarePlugin(
    id = "opinionated",
    mainClass = "common.OpinionatedDefaultsPlugin",
    displayName = "Opinionated JVM Defaults",
    description = "Some basic configuration for JVM projects"
)
