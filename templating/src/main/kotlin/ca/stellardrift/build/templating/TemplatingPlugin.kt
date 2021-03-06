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
package ca.stellardrift.build.templating

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JvmEcosystemPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal val SOURCE_FILES = listOf("**/*.java", "**/*.kt", "**/*.groovy", "**/*.scala")

open class GenerateTemplateTask : Copy() {
    @Internal
    var hasProperties = false

    init {
        group = "generation"
        description = "Generate source templates"
    }

    fun includeRoot(vararg sourceRoots: Any) {
        from(sourceRoots) {
            it.include(SOURCE_FILES)
        }
    }

    fun properties(vararg properties: Pair<String, Any?>) {
        properties(mapOf(*properties))
    }

    fun properties(properties: Map<String, Any?>) {
        hasProperties = true
        expand(properties)
    }
}

class TemplatingPlugin : Plugin<Project> {

    private fun whenJvmEcosystemPresent(target: Project) = with(target) {
        extensions.getByType<SourceSetContainer>().configureEach { src ->
            val taskName = src.getTaskName("generate", "Templates")
            val task = tasks.register<GenerateTemplateTask>(taskName) {
                val output = project.layout.buildDirectory.dir("generated-src/${src.name}/templates")
                includeRoot(file("src/${src.name}/templates"))
                into(output)
            }

            plugins.withId("kotlin") {
                /*extensions.getByType(KotlinSourceSetContainer::class.java).sourceSets.getByName(src.name)
                    .apply {
                        kotlin.srcDir(task.map { it.outputs })
                    }*/
                tasks.named(src.getCompileTaskName("Kotlin")).configure { t ->
                    t.dependsOn(task)
                }
            }

            plugins.withType(JavaPlugin::class.java) {
                src.java.srcDir(task.map { it.outputs })
                tasks.named(src.compileJavaTaskName).configure { t ->
                    t.dependsOn(task)
                }
            }
        }

        afterEvaluate {
            tasks.withType(GenerateTemplateTask::class.java).configureEach {
                if (!it.hasProperties) {
                    it.properties("project" to project)
                }
            }
        }
    }

    override fun apply(target: Project) {
        target.plugins.withType(JvmEcosystemPlugin::class.java) { whenJvmEcosystemPresent(target) }
    }
}
