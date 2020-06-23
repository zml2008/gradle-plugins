rootProject.name = "gradle-plugins"

listOf("opinionated-common", "opinionated-fabric", "opinionated-kotlin",
      "localization", "templating", "configurate-transformations", "dependency-manifester").forEach {
  include(":$it")
  findProject(":$it")?.name = "gradle-plugin-$it"
}
