dependencies {
    api("net.kyori:indra-common:2.0.0-SNAPSHOT")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.1")
    api("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
}

indraPluginPublishing {
    plugin(
        id = "opinionated",
        mainClass = "ca.stellardrift.build.common.OpinionatedDefaultsPlugin",
        displayName = "Opinionated JVM Defaults",
        description = "Some basic configuration for JVM projects"
    )
}
