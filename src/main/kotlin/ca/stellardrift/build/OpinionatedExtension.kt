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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication
import java.io.Serializable
import java.net.URL

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
 * Represents a license that can be applied to software.
 *
 * The [shortName] parameter must be one of the license names supported by
 * [Bintray's](https://www.jfrog.com/confluence/display/BT/Bintray+REST+API#BintrayRESTAPI-DeleteDockerTag)
 * REST API
 */
data class License(val name: String, val shortName: String, val url: URL) : Serializable

open class OpinionatedExtension(objects: ObjectFactory) {
    /**
     * Applied to source compatibilty, target compatibilty, and Kotlin version
     */
    var javaVersion: JavaVersion = JavaVersion.VERSION_1_8

    /**
     * Create an automatic module name using the format `<group>.<name>`,
     * where name has all dashes replaced with dots.
     */
    var automaticModuleNames = false

    /**
     * Set up JUnit Jupiter task configurations and dependencies
     */
    internal var usesJUnit5 = false

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

    /**
     * Enable JUnit 5 setup
     */
    fun useJUnit5() {
        this.usesJUnit5 = true
    }

    fun apache2() =
        license.set(
                License(
                        "The Apache License, Version 2.0",
                        "Apache-2.0",
                        URL("http://www.apache.org/licenses/LICENSE-2.0")
                )
        )

    fun mit() = license.set(
            License(
                    "The MIT License",
                    "MIT",
                    URL("https://opensource.org/licenses/MIT")
            )
    )

    fun gpl3() = license.set(
            License(
                    "GNU General Public License, Version 3",
                    "GPL-3.0",
                    URL("https://www.gnu.org/licenses/gpl-3.0.html")
            )
    )

    fun agpl3() =
        license.set(
                License(
                        "GNU Affero General Public License, Version 3",
                        "AGPL-V3",
                        URL("https://www.gnu.org/licenses/agpl-3.0.html")
                )
        )
}

internal const val EXTENSION_NAME = "opinionated"

internal fun Project.getOrCreateExtension(): OpinionatedExtension {
    return extensions.findByType(OpinionatedExtension::class.java)
            ?: extensions.create(EXTENSION_NAME, OpinionatedExtension::class.java)
}
