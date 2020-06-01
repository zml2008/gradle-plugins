import ca.stellardrift.build.self.declarePlugin

dependencies {
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.0.2")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
}

declarePlugin(
    id = "opinionated",
    mainClass = "common.OpinionatedDefaultsPlugin",
    displayName = "Opinionated JVM Defaults",
    description = "Some basic configuration for JVM projects"
)

declarePlugin(
    id = "opinionated.publish",
    mainClass = "common.OpinionatedPublishingPlugin",
    displayName = "Opinionated Publishing",
    description = "Common publishing setup options"
)
