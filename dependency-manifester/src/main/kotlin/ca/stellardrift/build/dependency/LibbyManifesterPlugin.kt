/*
 * Copyright 2020 zml
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.stellardrift.build.dependency

import java.io.File
import java.security.MessageDigest
import java.util.Base64
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

/**
 * Generate a dependency manifest to be read by Libby.
 *
 */
class DependencyManifesterPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        extensions.findByType(SourceSetContainer::class.java)?.findByName("main")?.apply {
            tasks.register(getTaskName("generate", "DependencyManifest"), GenerateManifest::class.java).configure {
                it.sourceConfiguration.set(configurations.named(runtimeClasspathConfigurationName))
                it.destination.set(output.resourcesDir?.resolve("dependencies.json"))
            }
        }
    }
}

class DependencyHandlerExtension(objects: ObjectFactory) {
    val configurations = objects.namedDomainObjectList(Configuration::class.java)
    val repositories = objects.domainObjectContainer(ArtifactRepository::class.java)
}

class GenerateManifest : DefaultTask() {

    /**
     * Configuration to search (transitively) for dependencies
     */
    @Input
    val sourceConfiguration: Property<Configuration> = project.objects.property(Configuration::class.java)

    /**
     * The file to write dependency data to
     */
    @OutputFile
    val destination: RegularFileProperty = project.objects.fileProperty()


    @TaskAction
    fun generate() {
        val dependencies = sourceConfiguration.get().resolvedConfiguration.firstLevelModuleDependencies.asSequence()
            .flatMap { it.allModuleArtifacts.asSequence() }
            .distinctBy { it.moduleVersion }
            .map {
                val checksum = it.file.makeDigest().asBase64()
                val group = it.moduleVersion.id.group
                val artifact = it.moduleVersion.id.name
                val version = it.moduleVersion.id.version
                val classifier = it.classifier
            }

        val repositories = project.repositories.asSequence()
            .filterIsInstance(MavenArtifactRepository::class.java)
            .map { it.url }

    }

}

private const val ALGO_SHA256: String = "SHA-256"

internal fun File.makeDigest(algorithm: String = ALGO_SHA256): ByteArray {
    val digest = MessageDigest.getInstance(algorithm)
    this.inputStream().use {
        val bytes = ByteArray(2048)
        var read = it.read(bytes)
        while (read != -1) {
            digest.update(bytes, 0, read)
            read = it.read(bytes)
        }
    }
    return digest.digest()
}

internal fun ByteArray.asBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}
