import ca.stellardrift.build.common.configurate
import ca.stellardrift.build.common.sonatypeOss
import ca.stellardrift.build.self.declarePlugin
plugins {
    groovy
}

repositories {
    sonatypeOss()
}

dependencies {
    implementation(localGroovy())
    api(platform(configurate("bom", "4.0.0-SNAPSHOT")))
    api(configurate("extra-kotlin"))
    api(configurate("hocon"))
    api(configurate("yaml"))
    api(configurate("gson"))
    api(configurate("xml"))
}

declarePlugin(
    id = "configurate-transformations",
    mainClass = "transformations.ConfigurateTransformationsPlugin",
    description = "File transformations for the Copy task using Configurate",
    displayName = "Configurate Transformations"
)
