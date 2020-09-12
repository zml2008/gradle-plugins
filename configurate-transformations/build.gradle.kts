import ca.stellardrift.build.common.configurate
import ca.stellardrift.build.self.declarePlugin

dependencies {
    api(platform(configurate("bom", "3.7")))
    implementation(configurate("ext-kotlin"))
    implementation(configurate("hocon"))
    implementation(configurate("yaml"))
    implementation(configurate("gson"))
    implementation(configurate("xml"))
}

declarePlugin(
    id = "configurate-transformations",
    mainClass = "transformations.ConfigurateTransformationsPlugin",
    description = "File transformations for the Copy task using Configurate",
    displayName = "Configurate Transformations"
)
