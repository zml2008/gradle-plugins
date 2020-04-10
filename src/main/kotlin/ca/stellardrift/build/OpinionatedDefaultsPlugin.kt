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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import net.minecrell.gradle.licenser.LicenseExtension
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.ajoberstar.grgit.gradle.GrgitPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import java.io.Serializable
import java.net.URL
import java.time.format.DateTimeFormatter

private val UTF_8 = "UTF-8"
private val PUBLICATION_ID = "maven"
internal val sourceFiles = listOf("**/*.java", "**/*.kt", "**/*.groovy", "**/*.scala")

/**
 * Options that configure how a SCM system is represented in a project's POM
 */
interface ScmOptions : Serializable {
    val website: String
    val scmWeb: String
    val connection: String
    val developerConnection: String
    val issueTrackerName: String
    val issueTrackerURL: String
}


data class GitHubOptions(
    val userName: String,
    val repo: String,
    override val website: String = "https://github.com/$userName/$repo"
) : ScmOptions {
    override val scmWeb = "https://github.com/$userName/$repo"
    override val connection = "scm:git:https://github.com/$userName/$repo"
    override val developerConnection = "scm:git:ssh://git@github.com/$userName/$repo"
    override val issueTrackerName = "GitHub"
    override val issueTrackerURL = "https://github.com/$userName/$repo/issues"
}

data class GitlabOptions(
    val userName: String,
    val repo: String,
    override val website: String = "https://gitlab.com/$userName/$repo"
) : ScmOptions {
    override val scmWeb = "https://gitlab.com/$userName/$repo"
    override val connection = "scm:git:https://gitlab.com/$userName/$repo"
    override val developerConnection = "scm:git:ssh://git@gitlab.com/$userName/$repo"
    override val issueTrackerName = "Gitlab"
    override val issueTrackerURL = "https://gitlab.com/$userName/$repo/-/issues"
}

/**
 * Represents a license that can be applied to software
 */
data class License(val name: String, val shortName: String, val url: URL) : Serializable

fun apache2() =
    License("The Apache License, Version 2.0", "Apache-2.0", URL("http://www.apache.org/licenses/LICENSE-2.0"))

fun mit() = License("The MIT License", "MIT", URL("https://opensource.org/licenses/MIT"))
fun gpl3() = License("GNU General Public License, Version 3", "GPL3", URL("https://www.gnu.org/licenses/gpl-3.0.html"))
fun agpl3() =
    License("GNU Affero General Public License, Version 3", "AGPL3", URL("https://www.gnu.org/licenses/agpl-3.0.html"))


open class OpinionatedExtension(objects: ObjectFactory) {
    /**
     * Applied to source compatibilty, target compatibilty, and Kotlin version
     */
    var javaVersion: JavaVersion = JavaVersion.VERSION_1_8

    /**
     * The primary publication for this project
     */
    lateinit var publication: MavenPublication internal set

    /**
     * A property that can automatically fill out pom options for common SCM systems.
     * While this will take any implementation of ScmOptions,
     * take a look at [github] and [gitlab] for some popular sites.
     */
    val scm: Property<ScmOptions> = objects.property(ScmOptions::class.java)

    val license: Property<License> = objects.property(License::class.java)

    /**
     * Reference a GitHub repository in the pom
     */
    fun github(userName: String, repo: String, website: String) {
        scm.set(GitHubOptions(userName, repo, website))
    }

    /**
     * Reference a Gitlab repository in the pom
     */
    fun gitlab(userName: String, repo: String, website: String) {
        scm.set(GitlabOptions(userName, repo, website))
    }

    /**
     * Reference a GitHub repository in the pom, with a website set to the GitHub project page
     */
    fun github(userName: String, repo: String) {
        scm.set(GitHubOptions(userName, repo))
    }

    /**
     * Reference a Gitlab repository in the pom, with a website set to the Gitlab project page
     */
    fun gitlab(userName: String, repo: String) {
        scm.set(GitlabOptions(userName, repo))
    }
}

class OpinionatedDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.create("opinionated", OpinionatedExtension::class.java)

            plugins.apply {
                apply("net.minecrell.licenser")
                apply("java-library")
                apply(GrgitPlugin::class.java)
                apply(BintrayPlugin::class.java)
                apply(MavenPublishPlugin::class.java)
            }

            val grgit = project.extensions.getByType(Grgit::class.java)

            project.version = rootProject.version
            project.group = rootProject.group

            extensions.findByType(LicenseExtension::class.java)?.apply {
                exclude {
                    it.file.startsWith(buildDir)
                }
                include(sourceFiles)
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

            // Test
            tasks.withType(Test::class.java).configureEach {
                it.useJUnitPlatform()
            }

            val requireClean = project.tasks.register("requireClean", RequireClean::class.java)
            extensions.getByType(PublishingExtension::class.java).run {
                publications.register(PUBLICATION_ID, MavenPublication::class.java) {
                    configureMavenPublication(this@with, extension, it)
                }
            }

            val tagRef = grgit.repository.jgit.tagList().call().firstOrNull()
            val tag: Tag? = grgit.resolve.toTag(tagRef?.name)
            val headCommit = grgit.head()

            val bintrayExtension = extensions.getByType(BintrayExtension::class.java).apply {
                user = findProperty("bintrayUser") as String? ?: System.getenv("BINTRAY_USER")
                key = findProperty("bintrayKey") as String? ?: System.getenv("BINTRAY_KEY")
                publish = true
                pkg.apply {
                    repo = findProperty("bintrayRepo") as String? ?: System.getenv("BINTRAY_REPO")
                    name = project.name
                    vcsUrl = extension.scm.orNull?.connection
                    version.apply {
                        name = project.version as String
                        vcsTag = tag?.name
                        desc = tag?.fullMessage
                        released = tag?.commit?.dateTime?.format(DateTimeFormatter.ISO_INSTANT)
                    }
                    setPublications(PUBLICATION_ID)
                }
            }

            tasks.named("bintrayUpload").configure {
                it.dependsOn(requireClean)
                it.onlyIf {
                    tag?.commit?.id.equals(headCommit?.id) &&
                            !(version as String).contains("SNAPSHOT")
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
                    }
                }

                tasks.withType(Javadoc::class.java).configureEach {
                    it.options.apply {
                        encoding = UTF_8
                    }
                }

                // Make builds more reproducible
                tasks.withType(AbstractArchiveTask::class.java).configureEach {
                    it.isPreserveFileTimestamps = false
                    it.isReproducibleFileOrder = true
                }

                bintrayExtension.pkg.apply {
                    vcsUrl = extension.scm.orNull?.connection
                    extension.license.orNull?.apply {
                        setLicenses(shortName)
                    }
                }

            }
        }
    }

    private fun configureMavenPublication(
        project: Project,
        extension: OpinionatedExtension,
        publication: MavenPublication
    ) {
        extension.publication = publication

        publication.apply {
            pom.apply {
                name.set(project.name)
                description.set(project.description)
                url.set(extension.scm.map { it.website })

                scm { p ->
                    p.connection.set(extension.scm.map { it.connection })
                    p.developerConnection.set(extension.scm.map { it.developerConnection })
                    p.url.set(extension.scm.map { it.scmWeb })
                }

                issueManagement { p ->
                    p.system.set(extension.scm.map { it.issueTrackerName })
                    p.url.set(extension.scm.map { it.issueTrackerURL })
                }

                licenses { p ->
                    p.license { l ->
                        l.name.set(extension.license.map { it.name })
                        l.url.set(extension.license.map { it.url.toString() })
                    }
                }
            }
        }
    }
}

open class RequireClean : DefaultTask() {
    init {
        group = "verification"
    }

    @TaskAction
    fun check() {
        val grgit = project.extensions.getByType(Grgit::class.java)
        if (!grgit.status().isClean) {
            throw GradleException("Source root must be clean! Make sure your changes are committed")
        }
    }
}

fun DependencyHandler.apAnd(scope: String, spec: String, configure: Dependency.() -> Unit = {}) = run {
    add("annotationProcessor", spec)?.apply(configure)
    add(scope, spec)?.apply(configure)
}

@Suppress("UNCHECKED_CAST")
fun <T : Dependency> DependencyHandler.apAnd(scope: String, spec: T, configure: T.() -> Unit = {}) = run {
    add("annotationProcessor", spec)?.apply { configure(this as T) }
    add(scope, spec)?.apply { configure(this as T) }
}
