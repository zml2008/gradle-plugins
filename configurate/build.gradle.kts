plugins {
    groovy
    alias(libs.plugins.eclipseApt)
}

dependencies {
    implementation(localGroovy())

    compileOnlyApi(variantOf(libs.immutables.value) { classifier("annotations") })
    compileOnlyApi(libs.immutables.builder)
    annotationProcessor(libs.immutables.value)

    api(platform(libs.configurate.bom))
    api(libs.configurate.extraKotlin)
    api(libs.configurate.hocon)
    api(libs.configurate.yaml)
    api(libs.configurate.gson) {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    implementation(libs.gson)
    api(libs.configurate.xml)
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
    processResources {
        inputs.property("version", project.version)

        expand(mutableMapOf("version" to rootProject.version))
    }

    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            links(
                "https://configurate.aoeu.xyz/${libs.versions.configurate.get()}/apidocs/"
            )
        }
    }
}
