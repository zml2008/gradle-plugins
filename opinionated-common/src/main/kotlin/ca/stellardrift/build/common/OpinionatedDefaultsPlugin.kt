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
package ca.stellardrift.build.common

import net.kyori.indra.repository.Repositories
import java.util.Locale
import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType

class OpinionatedDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = getOrCreateOpinionatedExtension()

            plugins.apply {
                apply("net.kyori.indra")
            }

            val headerFile = rootProject.file("LICENSE_HEADER")
            if (headerFile.isFile) {
                apply(plugin = "net.kyori.indra.license-header")

                extensions.configure(LicenseExtension::class.java) {
                    it.exclude {
                        it.file.startsWith(buildDir)
                    }
                    it.header(headerFile)
                }
            }

            // add useful repos
            Repositories.registerRepositoryExtensions(repositories, MINECRAFT_REPOSITORIES)

            tasks.withType(JavaCompile::class.java) {
                it.options.compilerArgs.add("-Xlint:-processing")
            }

            tasks.withType<Javadoc>().configureEach {
                val options = it.options
                if (options is StandardJavadocDocletOptions) {
                    options.linkSource()
                }
            }

            if (file(".checkstyle/checkstyle.xml").isFile) {
                apply(plugin = "net.kyori.indra.checkstyle")
            }

            afterEvaluate {
                if (extension.automaticModuleNames) {
                    tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class).configure {
                        it.manifest.attributes(mapOf(
                            "Automatic-Module-Name"
                                    to "$group.${name.replace("-", ".").toLowerCase(Locale.ROOT)}"
                        ))
                    }
                }
            }
        }
    }
}
