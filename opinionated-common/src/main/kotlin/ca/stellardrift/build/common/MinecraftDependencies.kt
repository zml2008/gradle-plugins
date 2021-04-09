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

import net.kyori.indra.repository.RemoteRepository
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler

fun RepositoryHandler.cottonMc() = cottonMc.addTo(this)
private val cottonMc = RemoteRepository.releasesOnly("cottonMc", "https://server.bbkr.space/artifactory/libs-release")

fun RepositoryHandler.engineHub() = engineHub.addTo(this)
private val engineHub = RemoteRepository.all("engineHub", "https://maven.enginehub.org/repo/")

fun RepositoryHandler.minecraft() = minecraft.addTo(this)
private val minecraft = RemoteRepository.releasesOnly("minecraft", "https://libraries.minecraft.net/")

fun RepositoryHandler.paper() = paper.addTo(this)
private val paper = RemoteRepository.all("paper", "https://papermc.io/repo/repository/maven-public/")

fun RepositoryHandler.pex() = pex.addTo(this)
private val pex = RemoteRepository.all("pex", "https://repo.glaremasters.me/repository/permissionsex/")

fun RepositoryHandler.spigot() = spigot.addTo(this)
private val spigot = RemoteRepository.all("spigot", "https://hub.spigotmc.org/nexus/content/groups/public/")

fun RepositoryHandler.sponge() = sponge.addTo(this)
private val sponge = RemoteRepository.all("sponge", "https://repo.spongepowered.org/repository/maven-public/")

fun RepositoryHandler.spongeReleases() = spongeReleases.addTo(this)
private val spongeReleases = RemoteRepository.releasesOnly("spongeReleases", "https://repo.spongepowered.org/repository/releases/")

fun RepositoryHandler.spongeSnapshots() = spongeSnapshots.addTo(this)
private val spongeSnapshots = RemoteRepository.snapshotsOnly("spongeSnapshots", "https://repo.spongepowered.org/repository/snapshots/")

fun RepositoryHandler.stellardriftReleases() = stellardriftReleases.addTo(this)
private val stellardriftReleases = RemoteRepository.releasesOnly("stellardriftReleases", "https://repo.stellardrift.ca/repository/stable/")

fun RepositoryHandler.stellardriftSnapshots() = stellardriftSnapshots.addTo(this)
private val stellardriftSnapshots = RemoteRepository.snapshotsOnly("stellardriftSnapshots", "https://repo.stellardrift.ca/repository/snapshots/")

fun RepositoryHandler.velocityReleases() = velocityReleases.addTo(this)
private val velocityReleases = RemoteRepository.releasesOnly("velocityReleases", "https://repo.velocitypowered.com/releases/")

fun RepositoryHandler.velocitySnapshots() = velocitySnapshots.addTo(this)
private val velocitySnapshots = RemoteRepository.snapshotsOnly("velocitySnapshots", "https://repo.velocitypowered.com/snapshots/")

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
    minecraft,
    stellardriftReleases,
    stellardriftSnapshots
)

fun DependencyHandler.configurate(comp: String, version: Any? = null): String {
    return "org.spongepowered:configurate-$comp${if (version == null) "" else ":$version"}"
}

fun DependencyHandler.adventure(component: String, version: Any? = null): String {
    return "net.kyori:adventure-$component${if (version == null) "" else ":$version"}"
}
