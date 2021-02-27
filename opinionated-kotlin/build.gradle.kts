dependencies {
    implementation(project(":gradle-plugin-opinionated-common"))
    implementation(kotlin("stdlib-jdk8", "1.4.30"))
    implementation(kotlin("gradle-plugin", "1.4.30"))
}

indraPluginPublishing {
    plugin(
        "opinionated.kotlin",
        "ca.stellardrift.build.kotlin.OpinionatedKotlinDefaultsPlugin",
        "Opinionated Kotlin Defaults",
        "Some basic configuration for Kotlin projects"
    )
}
