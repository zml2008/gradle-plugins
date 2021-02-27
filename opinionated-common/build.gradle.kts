dependencies {
    api("net.kyori:indra-common:1.3.1")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.5.0")
    api("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
}

indraPluginPublishing {
    plugin(
        id = "opinionated",
        mainClass = "ca.stellardrift.build.common.OpinionatedDefaultsPlugin",
        displayName = "Opinionated JVM Defaults",
        description = "Some basic configuration for JVM projects"
    )
}
