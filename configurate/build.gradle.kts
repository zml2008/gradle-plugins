plugins {
    groovy
}

val configurateVersion = "4.0.0"
dependencies {
    implementation(localGroovy())

    compileOnlyApi("org.immutables:value:2.8.8:annotations")
    compileOnlyApi("org.immutables:builder:2.8.8")
    annotationProcessor("org.immutables:value:2.8.8")

    api(platform("org.spongepowered:configurate-bom:$configurateVersion"))
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
        displayName = "Configurate Transformations",
        tags = listOf("configuration", "verification", "xml", "yaml", "hocon")
    )
    plugin(
        id = "polyglot-dependencies",
        mainClass = "ca.stellardrift.build.configurate.dependencies.PolyglotDependenciesPlugin",
        description = "Additional file format support for the gradle 6.8 dependencies file",
        displayName = "Polyglot Dependencies",
        tags = listOf("polyglot", "dependency-management", "xml", "yaml", "hocon")
    )
}

tasks {
    withType(GroovyCompile::class) {
        options.release.set(8)
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    processResources {
        inputs.property("version", project.version)

        expand(mutableMapOf("version" to rootProject.version))
    }

    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            links(
                "https://configurate.aoeu.xyz/$configurateVersion/apidocs/"
            )
        }
    }
}
