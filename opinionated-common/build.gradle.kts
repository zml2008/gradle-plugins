dependencies {
    api(libs.indra.common)
    api(libs.indra.licenserSpotless)
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
