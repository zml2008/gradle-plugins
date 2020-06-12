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

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.sponge() = maven {
    it.name = "sponge"
    it.url = URI("https://repo.spongepowered.org/maven/")
}

fun RepositoryHandler.sk89q() = maven {
    it.name = "s89q"
    it.url = URI("https://maven.sk89q.com/repo/")
}

fun RepositoryHandler.jitpack() = maven {
    it.name = "jitpack"
    it.url = URI("https://jitpack.io/")
}

fun RepositoryHandler.velocity() = velocitySnapshots()

fun RepositoryHandler.velocitySnapshots() = maven {
    it.name = "velocity"
    it.url = URI("https://repo.velocitypowered.com/snapshots/")
    it.mavenContent { maven ->
        maven.snapshotsOnly()
    }
}

fun RepositoryHandler.velocityReleases() = maven {
    it.name = "velocity"
    it.url = URI("https://repo.velocitypowered.com/releases/")
    it.mavenContent { maven ->
        maven.releasesOnly()
    }
}

fun RepositoryHandler.spigot() = maven {
    it.name = "spigot"
    it.url = URI("https://hub.spigotmc.org/nexus/content/groups/public/")
}

fun RepositoryHandler.paper() = maven {
    it.name = "paper"
    it.url = URI("https://papermc.io/repo/repository/maven-public/")
}

fun RepositoryHandler.pex() = maven {
    it.name = "pex"
    it.url = URI("https://repo.glaremasters.me/repository/permissionsex/")
}

fun RepositoryHandler.sonatypeOss() = maven {
    it.name = "sonatype"
    it.url = URI("https://oss.sonatype.org/content/groups/public/")
    it.mavenContent { maven ->
        maven.snapshotsOnly()
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
