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

import ca.stellardrift.build.configurate.ConfigFormats
import ca.stellardrift.build.configurate.transformations.convertFormat
import com.google.common.collect.Iterables
import java.io.File
import java.lang.UnsupportedOperationException
import java.util.Locale
import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.configuration.providers.mappings.MojangMappingsDependency
import net.fabricmc.loom.task.AbstractRunTask
import net.fabricmc.loom.util.Constants
import net.kyori.indra.extension as indraExtension
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.IllegalDependencyNotation
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.process.CommandLineArgumentProvider

private const val TESTMOD_SOURCE_SET = "testmod"
internal val MIXIN_AP_ARGS = setOf("outRefMapFile", "defaultObfuscationEnv", "outMapFileNamedIntermediary", "inMapFileNamedIntermediary")

class OpinionatedFabricPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "ca.stellardrift.opinionated")
        apply(plugin = "fabric-loom")
        apply(plugin = "com.github.fudge.forgedflowerloom")

        val minecraft = extensions.getByType<LoomGradleExtension>()
        val indra = indraExtension(this)

        // Set up refmap
        minecraft.refmapName = "${project.name.toLowerCase(Locale.ROOT)}-refmap.json"

        // Testmod configuration
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
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
        }
        minecraft.unmappedModCollection.from(testModJar)

        applyMixinSourceSets(this, testModSet.get())

        // Set up mixin source sets
        applyMixinSourceSets(this, mainSourceSet.get())

        // Convert yaml files to josn
        tasks.withType(ProcessResources::class.java).configureEach {
            it.inputs.property("version", project.version)
            it.filteringCharset = "UTF-8" // TODO: this is in indra v2

            // Convert data files yaml -> json
            it.filesMatching(
                sequenceOf(
                    "fabric.mod",
                    "data/**/*",
                    "assets/**/*"
                ).flatMap { base -> sequenceOf("$base.yml", "$base.yaml") }
                    .toList()
            ) { file ->
                file.convertFormat(ConfigFormats.YAML, ConfigFormats.JSON)
                if (file.name.startsWith("fabric.mod")) {
                    file.expand(mutableMapOf("project" to it.project))
                }
                file.name = file.name.substringBeforeLast('.') + ".json"
            }
            // Convert pack meta, without changing extension
            it.filesMatching("pack.mcmeta") { it.convertFormat(ConfigFormats.YAML, ConfigFormats.JSON) }
        }

        // Enable coloured logging in Gradle run tasks in-ide
        tasks.withType(AbstractRunTask::class).configureEach {
            it.jvmArgumentProviders += CommandLineArgumentProvider {
                if (System.getProperty("idea.active")?.toBoolean() == true || // IntelliJ
                    System.getenv("TERM") != null || // linux terminals
                    System.getenv("WT_SESSION") != null) { // Windows terminal
                    listOf("-Dfabric.log.disableAnsi=false")
                } else {
                    listOf()
                }
            }
        }

        // Set up in-place mappings migration
        configureInPlaceMappingMigration(this)

        indra.includeJavaSoftwareComponentInPublications.set(false)
        indra.configurePublications(Action {
            val remapJar = tasks["remapJar"]
            val remapSourcesJar = tasks["remapSourcesJar"]
            it.suppressAllPomMetadataWarnings()

            it.artifact(tasks[mainSourceSet.get().jarTaskName]) { a ->
                a.classifier = "dev"
            }
            it.artifact(remapJar)

            it.artifact(tasks[mainSourceSet.get().sourcesJarTaskName]) { a ->
                a.builtBy(remapSourcesJar)
            }
            it.artifact(tasks[mainSourceSet.get().javadocJarTaskName])
        })

        afterEvaluate { proj ->
            // Automatically link to Fabric and Yarn JD
            val depLinks = mutableListOf<String>()
            proj.configurations.findByName(Constants.Configurations.MAPPINGS)?.findDependencyVersion("net.fabricmc", "yarn")?.also {
                depLinks += "https://maven.fabricmc.net/docs/yarn-$it"
            }
            proj.configurations.findByName(Constants.Configurations.MOD_COMPILE_CLASSPATH)
                ?.findDependencyVersion("net.fabricmc.fabric-api", "fabric-api")?.also {
                    depLinks += "https://maven.fabricmc.net/docs/fabric-api-$it"
                }

            if (!depLinks.isEmpty()) {
                proj.tasks.withType<Javadoc>().configureEach { jd ->
                    val options = jd.options
                    if (options is StandardJavadocDocletOptions) {
                        options.links?.addAll(depLinks)
                        options.tags(listOf("reason:m:Reason for overwrite:")) // Add Mixin @reason JD tag definition
                    }
                }
            }
        }
    }

    private fun Configuration.findDependencyVersion(group: String, name: String): String? {
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
        val sourceSets = project.extensions.getByType<SourceSetContainer>()

        // Accessor
        // can see minecraft + its dependencies + all project dependencies
        // can be seen by project
        val accessor = sourceSets.register(base.getTaskName(null, "accessor")) {
            sequenceOf(
                    SourceSet::getCompileOnlyConfigurationName,
                    SourceSet::getImplementationConfigurationName,
                    SourceSet::getRuntimeOnlyConfigurationName
            ).forEach { configGetter ->
                project.configurations.named(configGetter(it)) { config ->
                    config.extendsFrom(project.configurations.getByName(configGetter(base)))
                }
            }
            project.configurations.named(it.implementationConfigurationName) { c ->
                c.extendsFrom(project.configurations.getByName(Constants.Configurations.MINECRAFT_NAMED))
            }

            base.compileClasspath += it.output
            base.runtimeClasspath += it.output

            if (base.javadocTaskName in project.tasks.names) {
                project.tasks.named(base.javadocTaskName, Javadoc::class) {
                    classpath += it.output
                }
            }
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
            it.tasks.named<JavaCompile>(accessor.get().compileJavaTaskName).configure { c ->
                c.options.compilerArgs.removeIf { arg -> arg.startsWith(refmapArgName) }
                c.options.compilerArgs.add("$refmapArgName=${c.destinationDir}/${project.name.toLowerCase()}-${accessor.get().name}-refmap.json")
            }
            // And remove Mixin AP parameters from the main source set
            it.tasks.named<JavaCompile>(base.compileJavaTaskName).configure { c ->
                c.options.compilerArgs.removeIf { arg -> MIXIN_AP_ARGS.any { mixin -> arg.contains(mixin) } }
            }
        }

        // Make sure Mixin AP doesn't run on the base source set
        project.configurations.named(base.annotationProcessorConfigurationName).configure {
            it.exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-mixin-compile-extensions"))
            it.exclude(mapOf("group" to "net.fabricmc", "module" to "sponge-mixin"))
            it.exclude(mapOf("group" to "org.spongepowered", "module" to "mixin"))
        }

        // Add source sets to jars
        try {
            project.tasks.named<Jar>(base.jarTaskName).configure {
                it.from(accessor.get().output)
                it.from(mixin.get().output)
            }
        } catch (_: UnknownTaskException) {
            // ignore, don't need to configure a task that doesn't exist
        }

        try {
            project.tasks.named<Jar>(base.sourcesJarTaskName).configure {
                accessor.get().allJava.forEach { f ->
                    if (f.exists()) {
                        it.from(f)
                    }
                }

                mixin.get().allJava.forEach { f ->
                    if (f.exists()) {
                        it.from(f)
                    }
                }
            }
        } catch (_: UnknownTaskException) {
            // ignore, don't need to configure a task that doesn't exist
        }
    }

    private fun configureInPlaceMappingMigration(target: Project) {
        val mappingsCache = target.gradle.sharedServices.registerIfAbsent("mappingsCache", MappingCache::class.java) {}

        // Generate mappings for old -> new
        val mappings: Provider<File> = target.providers.gradleProperty("newMappings")
            .forUseAtConfigurationTime()
            .map { mappingsName ->
                try {
                    if (mappingsName.startsWith(MojangMappingsDependency.GROUP + ':' + MojangMappingsDependency.MODULE + ':')) {
                        val loomExtension = target.extensions.getByType(LoomGradleExtension::class)
                        if (!mappingsName.endsWith(":" + loomExtension.minecraftProvider.minecraftVersion)) {
                            throw UnsupportedOperationException("Migrating Mojang mappings is currently only supported for the active minecraft version")
                        }
                        return@map MojangMappingsDependency(target, loomExtension).resolve()
                    } else {
                        val dependency = target.dependencies.create(mappingsName)
                        return@map target.configurations.detachedConfiguration(dependency).resolve()
                    }
                } catch (ignored: IllegalDependencyNotation) {
                    target.logger.info("Could not locate mappings, presuming V2 Yarn")
                    try {
                        return@map target.configurations.detachedConfiguration(
                            target.dependencies
                                .module(mapOf("group" to "net.fabricmc", "name" to "yarn", "version" to mappingsName, "classifier" to "v2"))
                        ).resolve()
                    } catch (ignored2: GradleException) {
                        target.logger.info("Could not locate mappings, presuming V1 Yarn")
                        return@map target.configurations.detachedConfiguration(
                            target.dependencies.module(mapOf("group" to "net.fabricmc", "name" to "yarn", "version" to mappingsName))
                        ).resolve()
                    }
                }
            }.map { Iterables.getOnlyElement(it) }

        val memoizedMappings = target.objects.fileProperty()
            .fileProvider(mappings)
        memoizedMappings.finalizeValueOnRead()

        // Then set up migrations
        val migrateInPlace = target.tasks.register("migrateMappingsAllInPlace", MigrateMappingsInPlace::class.java) {
            it.dependsOn(target.tasks.withType(RemapSourceSet::class))
        }

        val remapOutputDir = target.layout.buildDirectory.dir("remap-in-place")
        val outputDir = target.layout.buildDirectory.dir("remap")

        target.extensions.getByType(SourceSetContainer::class).all { set ->
            target.tasks.register("migrate${set.name.capitalize()}Mappings", RemapSourceSet::class.java) {
                it.sourceDirs.from(set.allJava.sourceDirectories)
                it.classpath.from(set.compileClasspath)
                it.sourceCompatibility.set(target.tasks.named(set.compileJavaTaskName, JavaCompile::class).map {
                    if (it.options.release.isPresent) {
                        it.options.release.get().toString()
                    } else if (it.sourceCompatibility != null) {
                        it.sourceCompatibility
                    } else {
                        val compiler = it.javaCompiler.orNull
                        if (compiler == null) {
                            JavaVersion.current().toString()
                        } else {
                            compiler.metadata.languageVersion.asInt().toString()
                        }
                    }
                })

                it.outputDirectory.set(outputDir.map { d -> d.dir(set.name) })
                it.remapLocations.set(remapOutputDir.map { d -> d.file("${it.name}.mappings") })

                it.mappingCache.set(mappingsCache)
                it.mappings.set(memoizedMappings)
                migrateInPlace.get().directoryMappings.from(it.remapLocations)
            }
        }

        target.tasks.register("migrateMappingsAll") {
            it.group = "stellardrift"
            it.description = "Remap all to official"
            it.dependsOn(target.tasks.withType(RemapSourceSet::class))
        }
    }
}
