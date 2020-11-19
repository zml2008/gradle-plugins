import ca.stellardrift.build.self.declarePlugin

dependencies {
    implementation(project(":gradle-plugin-opinionated-common"))
    implementation(kotlin("gradle-plugin", "1.4.10"))
}

declarePlugin(
    "opinionated.kotlin",
    "kotlin.OpinionatedKotlinDefaultsPlugin",
    "Opinionated Kotlin Defaults",
    "Some basic configuration for Kotlin projects"
)
