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

import groovy.lang.Closure
import java.net.URI
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor
import org.gradle.api.plugins.ExtensionAware

@Deprecated(message = "Old repository", replaceWith = ReplaceWith(expression = "spongeReleases()"))
fun RepositoryHandler.sponge() = maven {
    it.name = "sponge"
    it.url = URI("https://repo.spongepowered.org/maven/")
}

private val spongeSnapshots = RepositorySpec("spongeSnapshots", "https://repo-new.spongepowered.org/content/snapshots", ContentType.SNAPSHOTS)
private val spongeReleases = RepositorySpec("spongeReleases", "https://repo-new.spongepowered.org/content/releases", ContentType.RELEASES)

@Deprecated(message = "Old repository", replaceWith = ReplaceWith(expression = "engineHub()"))
fun RepositoryHandler.sk89q() = engineHub()

fun RepositoryHandler.engineHub() = engineHub.addTo(this)
private val engineHub = RepositorySpec("engineHub", "https://maven.enginehub.org/repo/")

fun RepositoryHandler.jitpack() = jitpack.addTo(this)
private val jitpack = RepositorySpec("jitpack", "https://jitpack.io")

fun RepositoryHandler.velocity() = velocitySnapshots()

fun RepositoryHandler.velocitySnapshots() = velocitySnapshots.addTo(this)
private val velocitySnapshots = RepositorySpec("velocitySnapshots", "https://repo.velocitypowered.com/snapshots/", ContentType.SNAPSHOTS)

fun RepositoryHandler.velocityReleases() = velocityReleases.addTo(this)
private val velocityReleases = RepositorySpec("velocityReleases", "https://repo.velocitypowered.com/releases/", ContentType.RELEASES)

fun RepositoryHandler.spigot() = spigot.addTo(this)
private val spigot = RepositorySpec("spigot", "https://hub.spigotmc.org/nexus/content/groups/public/")

fun RepositoryHandler.paper() = paper.addTo(this)
private val paper = RepositorySpec("paper", "https://papermc.io/repo/repository/maven-public/")

fun RepositoryHandler.pex() = pex.addTo(this)
private val pex = RepositorySpec("pex", "https://repo.glaremasters.me/repository/permissionsex/")

fun RepositoryHandler.sonatypeOss() = sonatypeOss.addTo(this)
private val sonatypeOss = RepositorySpec("sonatypeOss", "https://oss.sonatype.org/content/groups/public/", ContentType.SNAPSHOTS)

fun RepositoryHandler.cottonMc() = cottonMc.addTo(this)
private val cottonMc = RepositorySpec("cottonMc", "https://server.bbkr.space/artifactory/libs-release", ContentType.RELEASES)

fun RepositoryHandler.minecraft() = minecraft.addTo(this)
private val minecraft = RepositorySpec("minecraft", "https://libraries.minecraft.net/")

private val allRepositories = listOf(
    spongeSnapshots,
    spongeReleases,
    engineHub,
    jitpack,
    velocitySnapshots,
    velocityReleases,
    spigot,
    paper,
    pex,
    sonatypeOss,
    cottonMc,
    minecraft
)

/**
 * Types of content than can be on a repository. Either [RELEASES] only, [SNAPSHOTS] only, or [EITHER]
 */
private enum class ContentType {
    RELEASES, SNAPSHOTS, EITHER
}

/**
 * A specification for a maven repository
 */
private data class RepositorySpec(val name: String, val url: URI, val type: ContentType = ContentType.EITHER) {
    // helper constructor
    constructor(name: String, url: String, type: ContentType = ContentType.EITHER) : this(name, URI(url), type)

    fun addTo(handler: RepositoryHandler): MavenArtifactRepository {
        return handler.maven {
            it.name = name
            it.url = url
            when (type) {
                ContentType.RELEASES -> it.mavenContent(MavenRepositoryContentDescriptor::releasesOnly)
                ContentType.SNAPSHOTS -> it.mavenContent(MavenRepositoryContentDescriptor::snapshotsOnly)
                else -> {} // no-op
            }
        }
    }
}

internal fun registerExtensions(handler: RepositoryHandler) {
    val extensions = handler as ExtensionAware
    allRepositories.forEach {
        extensions.extensions.add(it.name, object : Closure<Unit>(null, handler) {
            fun doCall() {
                it.addTo(this.owner as RepositoryHandler)
            }
        })
    }
}

fun DependencyHandler.configurate(comp: String, version: Any? = null): String {
    return "org.spongepowered:configurate-$comp${if (version == null) "" else ":$version"}"
}

fun DependencyHandler.kyoriText(comp: String, version: Any): String {
    return "net.kyori:text-$comp:$version"
}

fun DependencyHandler.adventure(component: String, version: Any? = null): String {
    return "net.kyori:adventure-$component${if (version == null) "" else ":$version"}"
}
