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

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.sponge() = maven {
    it.name = "sponge"
    it.url = URI("https://repo.spongepowered.org/maven")
}

fun RepositoryHandler.sk89q() = maven {
    it.name = "s89q"
    it.url = URI("https://maven.sk89q.com/repo/")
}

fun RepositoryHandler.jitpack() = maven {
    it.name = "jitpack"
    it.url = URI("https://jitpack.io")
}

fun RepositoryHandler.velocity() = velocitySnapshots()

fun RepositoryHandler.velocitySnapshots() = maven {
    it.name = "velocity"
    it.url = URI("https://repo.velocitypowered.com/snapshots")
}

fun RepositoryHandler.velocityReleases() = maven {
    it.name = "velocity"
    it.url = URI("https://repo.velocitypowered.com/releases")
}

fun RepositoryHandler.spigot() = maven {
    it.name = "spigot"
    it.url = URI("https://hub.spigotmc.org/nexus/content/groups/public/")
}

fun RepositoryHandler.paper() = maven {
    it.name = "paper"
    it.url = URI("https://papermc.io/repo/repository/maven-public")
}

fun DependencyHandler.configurate(comp: String, version: Any? = null): String {
    return "org.spongepowered:configurate-$comp${if (version == null) "" else ":$version"}"
}

fun DependencyHandler.kyoriText(comp: String, version: Any): String {
    return "net.kyori:text-$comp:$version"
}

/**
 * For fabric environments, both include a mod (in Jar-in-Jar), and expose as an api dependency
 */
fun DependencyHandler.apiInclude(spec: String, configure: ExternalModuleDependency.() -> Unit = {}) {
    add("modApi", spec)?.apply {
        (this as ExternalModuleDependency).apply(configure)
    }
    add("include", spec)?.apply {
        (this as ExternalModuleDependency).apply(configure)
    }
}

/**
 * For fabric environments, both include a mod (in Jar-in-Jar), and expose as an implementation dependency
 */
fun DependencyHandler.implementationInclude(spec: String, configure: ExternalModuleDependency.() -> Unit = {}) {
    add("modImplementation", spec)?.apply {
        (this as ExternalModuleDependency).apply(configure)
    }
    add("include", spec)?.apply {
        (this as ExternalModuleDependency).apply(configure)
    }
}

/**
 * For fabric environments, both include a mod (in Jar-in-Jar), and expose as an api dependency
 */
fun <T : Dependency> DependencyHandler.apiInclude(dependency: T): T {
    add("modApi", dependency)
    add("include", dependency)
    return dependency
}

/**
 * For fabric environments, both include a mod (in Jar-in-Jar), and expose as an implementation dependency
 */
fun <T : Dependency> DependencyHandler.implementationInclude(dependency: T): T {
    add("modImplementation", dependency)
    add("include", dependency)
    return dependency
}
