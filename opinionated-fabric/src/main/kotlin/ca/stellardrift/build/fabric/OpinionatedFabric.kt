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

package ca.stellardrift.build.fabric

import java.util.Locale
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.task.AbstractRunTask
import net.fabricmc.loom.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

private const val FABRIC_MOD_DESCRIPTOR = "fabric.mod.json"

class OpinionatedFabricPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins.apply {
            apply("ca.stellardrift.opinionated")
            apply("fabric-loom")
        }

        val minecraft = extensions.getByType(LoomGradleExtension::class.java)

        // Set up refmap
        minecraft.refmapName = "${project.name.toLowerCase(Locale.ROOT)}-refmap.json"

        // Testmod configuration
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
        val mainSourceSet = sourceSets.named("main")

        val testModSet = sourceSets.register("testmod") {
            it.compileClasspath += mainSourceSet.get().compileClasspath
            it.runtimeClasspath += mainSourceSet.get().runtimeClasspath
        }

        dependencies.add("testmodCompile", mainSourceSet.get().output)

        val testmodJar = tasks.register("testmodJar", Jar::class.java) {
            it.archiveClassifier.set("testmod-dev")
            it.group = "build"

            it.from(testModSet.get().output)
            minecraft.addUnmappedMod(it.archiveFile.get().asFile.toPath())
        }

        /*tasks.withType(ProcessResources::class.java).configureEach {
            it.filesMatching(FABRIC_MOD_DESCRIPTOR) { modJson -> // TODO: throws UnsupportedOperationException???
                modJson.expand(mapOf("project" to it.project))
            }
        }*/

        tasks.withType(AbstractRunTask::class.java).configureEach {
            it.dependsOn(testmodJar)
        }

        afterEvaluate { proj ->
            val depLinks = mutableListOf<String>()
            proj.configurations.findByName(Constants.MAPPINGS)?.findDependencyVersion("net.fabricmc", "yarn")?.also {
                depLinks += "https://maven.fabricmc.net/docs/yarn-$it"
            }
            proj.configurations.findByName(Constants.MOD_COMPILE_CLASSPATH)
                ?.findDependencyVersion("net.fabricmc.fabric-api", "fabric-api")?.also {
                    depLinks += "https://maven.fabricmc.net/docs/fabric-api-$it"
                }

            if (!depLinks.isEmpty()) {
                proj.tasks.withType(Javadoc::class.java).configureEach { jd ->
                    val options = jd.options
                    if (options is StandardJavadocDocletOptions) {
                        options.links?.addAll(depLinks)
                        options.tags(listOf("reason:m:Reason for overwrite:")) // Add Mixin @reason JD tag definition
                    }
                }
            }
        }
    }

    internal fun Configuration.findDependencyVersion(group: String, name: String): String? {
        allDependencies.forEach {
            if (it.group == group && it.name == name && it.version != null) {
                return it.version
            }
        }
        return null
    }
}
