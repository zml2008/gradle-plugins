pluginManagement {
    repositories {
        maven("https://repo.stellardrift.ca/repository/internal/") {
            name = "stellardriftReleases"
            mavenContent { releasesOnly() }
        }
        maven("https://repo.stellardrift.ca/repository/snapshots/") {
            name = "stellardriftSnapshots"
            mavenContent { snapshotsOnly() }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { pluginManagement.repositories.forEach { add(it) } }
}

rootProject.name = "gradle-plugins"

listOf(
    "opinionated-common",
    "localization",
    "templating",
    "configurate",
    "stellardrift-repository"
).forEach {
  include(":$it")
  findProject(":$it")?.name = "gradle-plugin-$it"
}
