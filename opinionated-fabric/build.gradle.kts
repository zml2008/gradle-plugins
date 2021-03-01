repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "fabric"
    }
}

dependencies {
    implementation(project(":gradle-plugin-opinionated-common")) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(project(":gradle-plugin-configurate"))
    implementation("net.fabricmc:fabric-loom:0.6-SNAPSHOT")
    implementation("io.github.fudge:forgedflowerloom:2.0.0")

    // In-place mapping migration implementation
    implementation("org.cadixdev:mercury:0.1.0-rc1")
    implementation("net.fabricmc:lorenz-tiny:3.0.0")
    implementation("net.fabricmc:tiny-remapper:0.3.2")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("com.google.guava:guava:30.1-jre") // matching loom's version
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
