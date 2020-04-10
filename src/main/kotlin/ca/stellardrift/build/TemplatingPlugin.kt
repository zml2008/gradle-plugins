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

package ca.stellardrift.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer

open class GenerateTemplateTask : Copy() {
    @Internal
    var hasProperties = false

    fun includeRoot(vararg sourceRoots: Any) {
        from(sourceRoots) {
            it.include(sourceFiles)
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
    override fun apply(target: Project) {
        with(target) {
            extensions.getByType(SourceSetContainer::class.java).configureEach { src ->
                    val taskName = src.getTaskName("generate", "Templates")
                    val task = tasks.register(taskName, GenerateTemplateTask::class.java) {
                        val output = project.layout.buildDirectory.dir("generated-src/${src.name}/templates")
                        it.includeRoot(file("src/${src.name}/templates"))
                        it.into(output)
                    }

                    if (plugins.hasPlugin("kotlin")) { // we are using kotlin
                        extensions.getByType(KotlinSourceSetContainer::class.java).sourceSets.getByName(src.name)
                            .apply {
                                kotlin.srcDir(task.map { it.outputs })
                            }
                        tasks.named(src.getCompileTaskName("Kotlin")) { t ->
                            t.dependsOn(task)
                        }

                    } else if (plugins.hasPlugin(JavaPlugin::class.java)) {
                        src.java.srcDir(task.map { it.outputs })
                        tasks.named(src.compileJavaTaskName) { t ->
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
    }
}
