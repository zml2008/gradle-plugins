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

import net.kyori.indra.RemoteRepository
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler

fun RepositoryHandler.sponge() = sponge.addTo(this)
private val sponge = RemoteRepository("sponge", "https://repo-new.spongepowered.org/repository/maven-public/")

fun RepositoryHandler.spongeSnapshots() = spongeSnapshots.addTo(this)
private val spongeSnapshots = RemoteRepository("spongeSnapshots", "https://repo-new.spongepowered.org/repository/snapshots/", releases = false)

fun RepositoryHandler.spongeReleases() = spongeReleases.addTo(this)
private val spongeReleases = RemoteRepository("spongeReleases", "https://repo-new.spongepowered.org/repository/releases/", snapshots = false)

fun RepositoryHandler.engineHub() = engineHub.addTo(this)
private val engineHub = RemoteRepository("engineHub", "https://maven.enginehub.org/repo/")

fun RepositoryHandler.velocitySnapshots() = velocitySnapshots.addTo(this)
private val velocitySnapshots = RemoteRepository("velocitySnapshots", "https://repo.velocitypowered.com/snapshots/", releases = false)

fun RepositoryHandler.velocityReleases() = velocityReleases.addTo(this)
private val velocityReleases = RemoteRepository("velocityReleases", "https://repo.velocitypowered.com/releases/", snapshots = false)

fun RepositoryHandler.spigot() = spigot.addTo(this)
private val spigot = RemoteRepository("spigot", "https://hub.spigotmc.org/nexus/content/groups/public/")

fun RepositoryHandler.paper() = paper.addTo(this)
private val paper = RemoteRepository("paper", "https://papermc.io/repo/repository/maven-public/")

fun RepositoryHandler.pex() = pex.addTo(this)
private val pex = RemoteRepository("pex", "https://repo.glaremasters.me/repository/permissionsex/")

fun RepositoryHandler.cottonMc() = cottonMc.addTo(this)
private val cottonMc = RemoteRepository("cottonMc", "https://server.bbkr.space/artifactory/libs-release", snapshots = false)

fun RepositoryHandler.minecraft() = minecraft.addTo(this)
private val minecraft = RemoteRepository("minecraft", "https://libraries.minecraft.net/")

internal val MINECRAFT_REPOSITORIES = listOf(
    sponge,
    spongeSnapshots,
    spongeReleases,
    engineHub,
    velocitySnapshots,
    velocityReleases,
    spigot,
    paper,
    pex,
    cottonMc,
    minecraft
)

fun DependencyHandler.configurate(comp: String, version: Any? = null): String {
    return "org.spongepowered:configurate-$comp${if (version == null) "" else ":$version"}"
}

fun DependencyHandler.adventure(component: String, version: Any? = null): String {
    return "net.kyori:adventure-$component${if (version == null) "" else ":$version"}"
}
