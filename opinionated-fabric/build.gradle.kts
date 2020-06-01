import ca.stellardrift.build.self.declarePlugin

repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "fabric"
    }
}

dependencies {
    implementation(project(":"))
    implementation("net.fabricmc:fabric-loom:0.2.7-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
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

