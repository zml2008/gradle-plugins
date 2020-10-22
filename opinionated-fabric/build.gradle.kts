import ca.stellardrift.build.self.declarePlugin

repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "fabric"
    }
}

dependencies {
    implementation(project(":gradle-plugin-opinionated-common"))
    implementation("net.fabricmc:fabric-loom:0.5-SNAPSHOT")
    implementation("io.github.fudge:forgedflowerloom:2.0.0")
}

declarePlugin(
    id = "opinionated.fabric",
    mainClass = "fabric.OpinionatedFabricPlugin",
    displayName = "Opinionated Fabric Defaults",
    description = """
      Opinionated defaults for mods on the Fabric platform.
      
      Requires the https://maven.fabricmc.net plugin repository to be declared
      """.trimIndent()
)
