indraPluginPublishing {
    plugin(
        "repository",
        "ca.stellardrift.build.repository.StellardriftRepositoryPlugin",
        "Stellardrift Repository",
        "Register the stellardrift repository as an extension available to projects",
        listOf("repository", "extension")
    )
}
