pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }

}

rootProject.name = "gradle-plugins"

listOf("opinionated-common", "opinionated-fabric", "opinionated-kotlin",
      "localization", "templating", "configurate").forEach {
  include(":$it")
  findProject(":$it")?.name = "gradle-plugin-$it"
}
