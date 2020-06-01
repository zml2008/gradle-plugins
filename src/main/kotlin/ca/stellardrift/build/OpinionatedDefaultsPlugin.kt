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

import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.StandardJavadocDocletOptions


class OpinionatedDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = getOrCreateExtension()

            plugins.apply {
                apply("net.minecrell.licenser")
                apply("java-library")
                apply("org.jlleitschuh.gradle.ktlint") // Useful even on non-Kotlin projects for buildscript formatting
            }

            project.version = rootProject.version
            project.group = rootProject.group

            extensions.findByType(LicenseExtension::class.java)?.apply {
                exclude {
                    it.file.startsWith(buildDir)
                }
                include(SOURCE_FILES)

                val headerFile = rootProject.file("LICENSE_HEADER")
                if (headerFile.isFile) {
                    header = headerFile
                }
            }

            // Generate + use javadoc and sources jars

            val java = extensions.getByType(JavaPluginExtension::class.java).apply {
                withJavadocJar()
                withSourcesJar()
            }

            tasks.named("build").configure {
                it.dependsOn(tasks.named("javadocJar"))
                it.dependsOn(tasks.named("sourcesJar"))
            }

            val checkstyleDir = rootProject.projectDir.resolve("etc/checkstyle")
            if (checkstyleDir.isDirectory && checkstyleDir.resolve("checkstyle.xml").isFile) {
                plugins.apply("checkstyle")
                extensions.configure(CheckstyleExtension::class.java) {
                    it.toolVersion = "8.32"
                    it.configDirectory.set(checkstyleDir)
                    it.configProperties = mapOf(
                        "severity" to "error"
                    )
                }
            }

            afterEvaluate {
                java.apply {
                    sourceCompatibility = extension.javaVersion
                    targetCompatibility = extension.javaVersion
                }

                // Make sure we have a consistent encoding
                tasks.withType(JavaCompile::class.java).configureEach {
                    it.options.apply {
                        encoding = UTF_8
                        compilerArgs.addAll(
                            listOf(
                                "-Xlint:all",
                                "-Xlint:-serial",  // ignore missing serialVersionUID
                                "-Xlint:-processing" // don't warn when annotation processors aren't claimed
                            )
                        )
                        if (JavaVersion.toVersion(it.toolChain.version).isJava9Compatible) {
                            compilerArgs.addAll(listOf(
                                "-Xdoclint", "-Xdoclint:-missing", // javadoc: warn about everything except missing comment (broken on JDK8)
                                "--release", extension.javaVersion.majorVersion // enfore class availablity
                            ))
                        }
                    }
                }

                tasks.withType(Javadoc::class.java).configureEach {
                    val options = it.options
                    val version = JavaVersion.toVersion(it.toolChain.version)
                    if (version == JavaVersion.VERSION_12) {
                        throw GradleException("Javadoc cannot be generated on JDK 12 -- " +
                                "see https://bugs.openjdk.java.net/browse/JDK-8222091")
                    }
                    options.encoding = UTF_8
                    if (options is StandardJavadocDocletOptions) {
                        options.source = extension.javaVersion.toString()
                        if (version.isJava9Compatible) {
                            options.addBooleanOption("html5", true)
                        }
                        options.links(extension.javaVersion.javaDocLinkUrl)
                        options.linkSource()
                    }
                }

                // Make builds more reproducible
                tasks.withType(AbstractArchiveTask::class.java).configureEach {
                    it.isPreserveFileTimestamps = false
                    it.isReproducibleFileOrder = true
                }


                if (extension.automaticModuleNames) {
                    tasks.named("jar", Jar::class.java).configure {
                        it.manifest.attributes(mapOf("Automatic-Module-Name" to "$group.${name.replace("-", ".")}"))
                    }
                }

                if (extension.usesJUnit5) {
                    tasks.withType(Test::class.java).configureEach {
                        it.useJUnitPlatform()
                    }

                    extensions.getByType(SourceSetContainer::class.java).named("test").configure {
                        dependencies.apply {
                            val junitVersion = findProperty(VERSION_JUNIT_PROPERTY) ?: VERSION_JUNIT_DEFAULT
                            add(
                                it.implementationConfigurationName,
                                "org.junit.jupiter:junit-jupiter-api:$junitVersion"
                            )
                            add(
                                it.runtimeOnlyConfigurationName,
                                "org.junit.jupiter:junit-jupiter-engine:$junitVersion"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Deprecated(message = "Use nested dependencies", replaceWith = ReplaceWith("annotationProcessor(\"\$scope\"(spec, configure)!!)"))
fun DependencyHandler.apAnd(scope: String, spec: String, configure: Dependency.() -> Unit = {}) = run {
    add("annotationProcessor", spec)?.apply(configure)
    add(scope, spec)?.apply(configure)
}

@Deprecated(message = "Use nested dependencies", replaceWith = ReplaceWith("annotationProcessor(\"\$scope\"(spec, configure)!!)"))
@Suppress("UNCHECKED_CAST")
fun <T : Dependency> DependencyHandler.apAnd(scope: String, spec: T, configure: T.() -> Unit = {}) = run {
    add("annotationProcessor", spec)?.apply { configure(this as T) }
    add(scope, spec)?.apply { configure(this as T) }
}

internal val JavaVersion.javaDocLinkUrl: String get() {
    return if (isJava11Compatible) {
        "https://docs.oracle.com/en/java/javase/$majorVersion/docs/api"
    } else {
        "https://docs.oracle.com/javase/$majorVersion/docs/api"
    }
}
