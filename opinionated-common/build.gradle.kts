dependencies {
    api("net.kyori:indra-common:2.0.0")
    api("gradle.plugin.org.cadixdev.gradle:licenser:0.6.0")
}

indraPluginPublishing {
    plugin(
        "opinionated",
        "ca.stellardrift.build.common.OpinionatedDefaultsPlugin",
        "Opinionated JVM Defaults",
        "Some basic configuration for JVM projects"
    )
}
