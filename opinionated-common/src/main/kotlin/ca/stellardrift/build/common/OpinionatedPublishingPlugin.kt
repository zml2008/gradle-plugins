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

import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import java.time.format.DateTimeFormatter
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.ajoberstar.grgit.gradle.GrgitPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

private val PUBLICATION_ID = "maven"
private val DATE_FORMAT_BINTRAY = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

class OpinionatedPublishingPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<GrgitPlugin>()
        apply<BintrayPlugin>()
        apply<MavenPublishPlugin>()
        apply<SigningPlugin>()

        val extension = getOrCreateOpinionatedExtension()

        val requireClean = project.tasks.register<RequireClean>("requireClean")
        val publications = extensions.getByType<PublishingExtension>().run {
            publications.register(PUBLICATION_ID, MavenPublication::class.java) {
                configureMavenPublication(this@with, extension, it)
            }
            publications
        }

        extensions.getByType<SigningExtension>().apply {
            useGpgCmd()
            sign(publications)
        }

        tasks.withType<Sign>().configureEach {
            it.onlyIf {
                hasProperty("forceSign") || isRelease()
            }
        }

        val bintrayExtension = extensions.getByType<BintrayExtension>().apply {
            user = findProperty("bintrayUser") as String? ?: System.getenv("BINTRAY_USER")
            key = findProperty("bintrayKey") as String? ?: System.getenv("BINTRAY_KEY")
            publish = true
            pkg.apply {
                repo = findProperty("bintrayRepo") as String? ?: System.getenv("BINTRAY_REPO")
                name = project.name
                vcsUrl = extension.scm.orNull?.connection
                version.apply {
                    val tag: Tag? = grgit?.headTag()
                    vcsTag = tag?.name
                    desc = tag?.shortMessage
                    released = tag?.commit?.dateTime?.format(DATE_FORMAT_BINTRAY)
                }
                setPublications(PUBLICATION_ID)
            }
        }

        tasks.named("bintrayUpload").configure {
            it.dependsOn(requireClean)
            it.onlyIf {
                isRelease()
            }
        }

        afterEvaluate {
            bintrayExtension.pkg.apply {
                vcsUrl = extension.scm.orNull?.scmWeb + ".git"
                version.name = project.version as String
                extension.license.orNull?.apply {
                    setLicenses(shortName)
                }
            }

            val publishing = extensions.getByType<PublishingExtension>()
            extension.stagedRepositories.forEach {
                if (!it.snapshotsOnly || !project.version.toString().contains("-SNAPSHOT")) {
                    publishing.repositories.maven { repo ->
                        repo.name = it.id
                        repo.url = it.url
                        repo.credentials(PasswordCredentials::class)
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

/**
 * Require that the project has no files that are uncommitted to SCM,
 * to prevent accidentally publishing content that does not match the
 * published source.
 */
open class RequireClean : DefaultTask() {
    init {
        group = "verification"
    }

    @TaskAction
    fun check() {
        if (project.grgit?.status()?.isClean == false) {
            throw GradleException("Source root must be clean! Make sure your changes are committed")
        }
    }
}

/**
 * Verify that this project is checked out to a release version, meaning that:
 *
 * - The version does not contain SNAPSHOT
 * - The project is managed within a Git repository
 * - the current head commit is tagged
 */
fun Project.isRelease(): Boolean {
    val tag = (grgit ?: return false).headTag()

    return tag != null &&
        !(version as String).contains("SNAPSHOT")
}

private val Project.grgit get() = extensions.findByType(Grgit::class.java)

/**
 * Find a tag, if any, that corresponds with the current checked out commit
 */
fun Grgit.headTag(): Tag? {
    val headCommit = head()
    return tag.list().find { it.commit == headCommit }
}
