/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.2.2/userguide/multi_project_builds.html
 */

rootProject.name = "gradle-plugins"

listOf("opinionated-common", "opinionated-fabric", "opinionated-kotlin",
      "localization", "templating").forEach {
  include(":$it")
  findProject(":$it")?.name = "gradle-plugin-$it"
}
