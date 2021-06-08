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
package ca.stellardrift.build.localization

import groovy.text.StreamingTemplateEngine
import java.io.FileReader
import java.io.FileWriter
import java.util.Locale
import java.util.Properties
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JvmEcosystemPlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

const val MESSAGES_ROOT_NAME = "messages"

enum class TemplateType(val extension: String) {
    JAVA("java"), KOTLIN("kt"), OTHER("fixme")
}

open class LocalizationExtension @Inject constructor(objects: ObjectFactory) {
    val templateFile: RegularFileProperty = objects.fileProperty()
    val templateType: Property<TemplateType> = objects.property(TemplateType::class.java).convention(TemplateType.KOTLIN)
}

abstract class LocalizationGenerate : DefaultTask() {
    private val templateEngine = StreamingTemplateEngine()

    @get:Inject
    abstract val objectFactory: ObjectFactory

    @get:InputDirectory
    @get:SkipWhenEmpty
    abstract val resourceBundleSources: DirectoryProperty

    private val tree = objectFactory.fileTree().from(resourceBundleSources)

    @get:InputFile
    abstract val templateFile: RegularFileProperty

    @get:Input
    abstract val templateType: Property<TemplateType>

    @get:OutputDirectory
    abstract val generatedSourcesOut: DirectoryProperty

    init {
        tree.include("**/*.properties")
        tree.exclude { it2 -> it2.name.contains('_') } // Don't include already translated files
        group = "generation"
        description = "Generate source files for resource bundle constants, based on a template and a resource bundle root"
    }

    @TaskAction
    fun generateSources() {
        val template = templateEngine.createTemplate(templateFile.get().asFile)
        tree.visit {
            if (it.isDirectory) {
                return@visit
            }
            val path = it.relativePath

            val packageName = path.parent.segments.joinToString(".").toLowerCase(Locale.ROOT)
            val className = path.lastName.split('.', limit = 2).first().capitalize()
            val destinationPath = path.replaceLastName("$className.${templateType.get().extension}").getFile(generatedSourcesOut.asFile.get())

            val propertiesFile = Properties()
            FileReader(it.file).use { read ->
                propertiesFile.load(read)
            }

            val templateData = mapOf(
                "bundleName" to "$packageName.${path.lastName.split('.', limit = 2).first()}",
                "packageName" to packageName,
                "className" to className,
                "keys" to propertiesFile.keys
            )

            destinationPath.parentFile.mkdirs()
            FileWriter(destinationPath).use { write ->
                template.make(templateData).writeTo(write)
            }
        }
    }
}

/**
 * The localization plugin takes a source tree of `src/<sourceSet>/messages` containing resource bundle properties files,
 * and generates one class per resource bundle with a field for each key in the bundle.
 *
 * The template file used
 *
 * This becomes another resource root. In addition
 */
class LocalizationPlugin : Plugin<Project> {

    private fun actuallyApply(project: Project, extension: LocalizationExtension, parentTask: TaskProvider<Task>): Unit = with(project) {
        val sSets = extensions.getByType<SourceSetContainer>()
        sSets.configureEach {
            val messagesFileBasedir = project.file("src/${it.name}/$MESSAGES_ROOT_NAME")
            val outDir = project.layout.buildDirectory.dir("generated-src/${it.name}/$MESSAGES_ROOT_NAME")
            val task = project.tasks.register<LocalizationGenerate>(it.getTaskName("generate", "Localization")).configure { loc ->
                loc.resourceBundleSources.set(messagesFileBasedir)
                loc.templateFile.set(extension.templateFile)
                loc.generatedSourcesOut.set(outDir)
                loc.templateType.set(extension.templateType)
            }
            parentTask.configure { t ->
                t.dependsOn(task)
            }

            it.resources.srcDir(messagesFileBasedir)
        }
        afterEvaluate { _ ->
            sSets.configureEach {
                val task = project.tasks.named<LocalizationGenerate>(it.getTaskName("generate", "Localization"))

                // TODO: Kotlin-specific?
                it.java.srcDir(task.map { t -> t.generatedSourcesOut })
                project.tasks.named(it.compileJavaTaskName) { compile ->
                    compile.dependsOn(task)
                }

                extension.templateType.finalizeValue()
                if (extension.templateType.get() == TemplateType.KOTLIN) {
                    plugins.withId("org.jetbrains.kotlin.jvm") { _ ->
                        project.tasks.findByName(it.getCompileTaskName("Kotlin"))?.dependsOn(task)
                    }
                }

                /*when (extension.templateType.get()) {
                TemplateType.KOTLIN -> {
                    project.extensions.findByType(KotlinSourceSetContainer::class.java)?.sourceSets?.named(it.name)
                        ?.configure { set ->
                            set.kotlin.srcDir(task.map { t -> t.generatedSourcesOut })
                            project.tasks.named(it.getCompileTaskName("Kotlin")) {
                                it.dependsOn(task)
                            }
                        }
                    }
                    TemplateType.JAVA -> {
                    }
                    else -> {
                        // no-op
                    }
                }
                it.resources.srcDir(task.map { t -> t.resourceBundleSources })
                 */
            }
        }
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create<LocalizationExtension>("localization")
        val parentTask = project.tasks.register("generateAllLocalizations")

        project.plugins.withType(JvmEcosystemPlugin::class.java) {
            actuallyApply(project, extension, parentTask)
        }
    }
}
