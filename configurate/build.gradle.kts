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
    api(platform("org.spongepowered:configurate-bom:4.0.0"))
    api("org.spongepowered:configurate-extra-kotlin")
    api("org.spongepowered:configurate-hocon")
    api("org.spongepowered:configurate-yaml")
    api("org.spongepowered:configurate-gson")
    api("org.spongepowered:configurate-xml")
}

indraPluginPublishing {
    plugin(
        id = "configurate-transformations",
        mainClass = "ca.stellardrift.build.configurate.transformations.ConfigurateTransformationsPlugin",
        description = "File transformations for the Copy task using Configurate",
        displayName = "Configurate Transformations"
    )
}
