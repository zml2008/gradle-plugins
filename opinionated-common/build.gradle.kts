dependencies {
    api("net.kyori:indra-common:3.0.0")
    api("net.kyori:indra-licenser-spotless:3.0.0")
    api(project(":gradle-plugin-stellardrift-repository"))
}

indraPluginPublishing {
    plugin(
        "opinionated",
        "ca.stellardrift.build.common.OpinionatedDefaultsPlugin",
        "Opinionated JVM Defaults",
        "Some basic configuration for JVM projects"
    )
}
