import ca.stellardrift.build.common.configurate

plugins {
    groovy
}

tasks.withType(GroovyCompile::class) {
    options.release.set(8)
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.processResources {
    inputs.property("version", project.version)

    expand(mutableMapOf("version" to rootProject.version))
}

dependencies {
    implementation(localGroovy())
    api(platform(configurate("bom", "4.0.0")))
    api(configurate("extra-kotlin"))
    api(configurate("hocon"))
    api(configurate("yaml"))
    api(configurate("gson"))
    api(configurate("xml"))
}

indraPluginPublishing {
    plugin(
        id = "configurate-transformations",
        mainClass = "ca.stellardrift.build.configurate.transformations.ConfigurateTransformationsPlugin",
        description = "File transformations for the Copy task using Configurate",
        displayName = "Configurate Transformations"
    )
}
