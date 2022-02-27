plugins {
    groovy
    id("com.diffplug.eclipse.apt") version "3.35.0"
}

val configurateVersion = "4.1.2"
dependencies {
    implementation(localGroovy())

    compileOnlyApi("org.immutables:value:2.9.0:annotations")
    compileOnlyApi("org.immutables:builder:2.9.0")
    annotationProcessor("org.immutables:value:2.9.0")

    api(platform("org.spongepowered:configurate-bom:$configurateVersion"))
    api("org.spongepowered:configurate-extra-kotlin")
    api("org.spongepowered:configurate-hocon")
    api("org.spongepowered:configurate-yaml")
    api("org.spongepowered:configurate-gson") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    implementation("com.google.code.gson:gson:2.9.0")
    api("org.spongepowered:configurate-xml")
}

indraPluginPublishing {
    plugin(
        "configurate-transformations",
        "ca.stellardrift.build.configurate.transformations.ConfigurateTransformationsPlugin",
        "File transformations for the Copy task using Configurate",
        "Configurate Transformations",
        listOf("configuration", "verification", "xml", "yaml", "hocon")
    )
    plugin(
        "polyglot-version-catalogs",
        "ca.stellardrift.build.configurate.catalog.PolyglotVersionCatalogPlugin",
        "Additional file format support for the gradle 7.0 version catalog system",
        "Polyglot Version Catalogs",
        listOf("polyglot", "dependency-management", "xml", "yaml", "hocon")
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
