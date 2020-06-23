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

import ca.stellardrift.build.common.getOrCreateOpinionatedExtension
import java.util.Locale
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.task.AbstractRunTask
import net.fabricmc.loom.util.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

private const val FABRIC_MOD_DESCRIPTOR = "fabric.mod.json"
private const val TESTMOD_SOURCE_SET = "testmod"
internal val MIXIN_AP_ARGS = setOf("outRefMapFile", "defaultObfuscationEnv", "outMapFileNamedIntermediary", "inMapFileNamedIntermediary")

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

        val testModSet = sourceSets.register(TESTMOD_SOURCE_SET) {
            it.compileClasspath += mainSourceSet.get().compileClasspath
            it.runtimeClasspath += mainSourceSet.get().runtimeClasspath
            dependencies.add(it.implementationConfigurationName, mainSourceSet.get().output)
        }

        val testModJar = tasks.register(testModSet.get().jarTaskName, Jar::class.java) {
            it.archiveClassifier.set("$TESTMOD_SOURCE_SET-dev")
            it.group = "build"

            it.from(testModSet.get().output)
            minecraft.addUnmappedMod(it.archiveFile.get().asFile.toPath())
        }

        tasks.withType(AbstractRunTask::class.java).configureEach {
            it.dependsOn(testModJar)
        }

        applyMixinSourceSets(this, testModSet.get())

        // Set up mixin source sets
        applyMixinSourceSets(this, mainSourceSet.get())

        /*tasks.withType(ProcessResources::class.java).configureEach {
            it.filesMatching(FABRIC_MOD_DESCRIPTOR) { modJson -> // TODO: throws UnsupportedOperationException???
                modJson.expand(mapOf("project" to it.project))
            }
        }*/

        afterEvaluate { proj ->
            // Automatically link to Fabric and Yarn JD
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

            // Set up publishing to publish remapped, dev, sources, and JD jars
            proj.getOrCreateOpinionatedExtension().publication?.apply {
                val remapJar = proj.tasks.getByName("remapJar")
                val remapSourcesJar = proj.tasks.getByName("remapSourcesJar")
                suppressAllPomMetadataWarnings()

                artifact(tasks.getByName(mainSourceSet.get().jarTaskName)) {
                    it.classifier = "dev"
                }
                artifact(remapJar)

                artifact(tasks.getByName(mainSourceSet.get().sourcesJarTaskName)) {
                    it.builtBy(remapSourcesJar)
                }
                artifact(tasks.getByName(mainSourceSet.get().javadocJarTaskName))
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

    /**
     * Create companion source sets to the base source set for mixins and accessors.
     *
     * - The base source set will no longer have access to Mixin
     * - The `accessor` sourceSet will be able to see Minecraft but won't be able to see any of the rest of the project.
     *   Classes in the `accessor` sourceSet will be visible to the project
     *
     * - The `mixin` sourceSet wil be able to see Minecraft and the rest of the project.
     *   Classes in the `mixin` sourceSet won't be visible to the rest of the project.
     */
    private fun applyMixinSourceSets(project: Project, base: SourceSet) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        // Accessor
        // can see minecraft + its dependencies
        // can be seen by project
        val accessor = sourceSets.register(base.getTaskName(null, "accessor")) {
            project.configurations.named(it.implementationConfigurationName).configure { c ->
                c.extendsFrom(project.configurations.named(Constants.MINECRAFT_NAMED).get())
            }
            project.dependencies.add(base.implementationConfigurationName, it.output)
        }

        // Mixin
        // can see entire project
        // can't be seen by rest of project
        val mixin = sourceSets.register(base.getTaskName(null, "mixin")) {
            it.compileClasspath += base.compileClasspath
            it.runtimeClasspath += base.runtimeClasspath
            project.dependencies.add(it.implementationConfigurationName, base.output)
        }

        // TODO: Provide a nicer way to do this in Loom
        val refmapArgName = "-AoutRefMapFile"
        project.afterEvaluate {
            // configure refmap
            it.tasks.named(accessor.get().compileJavaTaskName, JavaCompile::class.java).configure { c ->
                c.options.compilerArgs.removeIf { arg -> arg.startsWith(refmapArgName) }
                c.options.compilerArgs.add("$refmapArgName=${c.destinationDir}/${project.name.toLowerCase()}-${accessor.get().name}-refmap.json")
            }
            // And remove Mixin AP parameters from the main source set
            it.tasks.named(base.compileJavaTaskName, JavaCompile::class.java).configure { c ->
                c.options.compilerArgs.removeIf { arg -> MIXIN_AP_ARGS.any { mixin -> arg.contains(mixin) } }
            }
        }

        // Make sure Mixin AP doesn't run on the base source set
        project.configurations.named(base.annotationProcessorConfigurationName).configure {
            it.exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-mixin-compile-extensions"))
            it.exclude(mapOf("group" to "net.fabricmc", "module" to "sponge-mixin"))
        }

        // Add source sets to jars
        try {
            project.tasks.named(base.jarTaskName, Jar::class.java).configure {
                it.from(accessor.get().output)
                it.from(mixin.get().output)
            }
        } catch (_: UnknownTaskException) {
            // ignore, don't need to configure a task that doesn't exist
        }

        try {
            project.tasks.named(base.sourcesJarTaskName, Jar::class.java).configure {
                it.from(accessor.get().allJava)
                it.from(mixin.get().allJava)
            }
        } catch (_: UnknownTaskException) {
            // ignore, don't need to configure a task that doesn't exist
        }
    }
}
