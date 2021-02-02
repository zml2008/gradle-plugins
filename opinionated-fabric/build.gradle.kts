repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "fabric"
    }
}

dependencies {
    implementation(project(":gradle-plugin-opinionated-common")) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("net.fabricmc:fabric-loom:0.5-SNAPSHOT")
    implementation("io.github.fudge:forgedflowerloom:2.0.0")
}

indraPluginPublishing {
    plugin(
        id = "opinionated.fabric",
        mainClass = "ca.stellardrift.build.fabric.OpinionatedFabricPlugin",
        displayName = "Opinionated Fabric Defaults",
        description = """
      Opinionated defaults for mods on the Fabric platform.
      
      Requires the https://maven.fabricmc.net plugin repository to be declared
      """.trimIndent()
    )
}
