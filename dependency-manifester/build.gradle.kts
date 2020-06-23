import ca.stellardrift.build.self.declarePlugin

plugins {
    kotlin("plugin.serialization") version "1.3.72"
}

dependencies {
    implementation("net.byteflux:libby-core:0.0.2-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}

declarePlugin(
    id="libby-manifester",
    mainClass = "dependency.LibbyManifesterPlugin",
    displayName = "Libby Dependency Manifest Generator"
)

