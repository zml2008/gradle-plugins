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

import java.util.Locale
import net.kyori.indra.isRelease
import net.kyori.indra.registerRepositoryExtensions
import org.cadixdev.gradle.licenser.LicenseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign

class OpinionatedDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = getOrCreateOpinionatedExtension()

            plugins.apply {
                apply("net.kyori.indra")
                apply("org.jlleitschuh.gradle.ktlint") // Useful even on non-Kotlin projects for buildscript formatting
            }

            tasks.withType<Sign>().configureEach {
                it.onlyIf {
                    hasProperty("forgeSign") || isRelease(it.project) // remove at indra 1.2
                }
            }

            val headerFile = rootProject.file("LICENSE_HEADER")
            if (headerFile.isFile) {
                apply(plugin = "net.kyori.indra.license-header")

                extensions.configure(LicenseExtension::class.java) {
                    it.exclude {
                        it.file.startsWith(buildDir)
                    }
                    it.header = headerFile
                }
            }

            // add useful repos
            registerRepositoryExtensions(repositories, MINECRAFT_REPOSITORIES)

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
                    tasks.named<Jar>("jar").configure {
                        it.manifest.attributes(mapOf("Automatic-Module-Name" to "$group.${name.replace("-", ".").toLowerCase(Locale.ROOT)}"))
                    }
                }
            }
        }
    }
}
