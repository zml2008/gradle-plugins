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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class OpinionatedKotlinDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(OpinionatedDefaultsPlugin::class.java)
            plugins.apply("kotlin")

            dependencies.apply {
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }

            val stellardriftExtension = getOrCreateExtension()
            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=enable")
            }
            afterEvaluate {
                tasks.withType(KotlinCompile::class.java).configureEach {
                    it.kotlinOptions.jvmTarget = stellardriftExtension.javaVersion.toString()
                }
            }
        }
    }
}

fun DependencyHandler.kaptAnd(scope: String, spec: String, configure: Dependency.() -> Unit = {}) = run {
    add("kapt", spec)?.apply(configure)
    add(scope, spec)?.apply(configure)
}

@Suppress("UNCHECKED_CAST")
fun <T : Dependency> DependencyHandler.kaptAnd(scope: String, spec: T, configure: T.() -> Unit = {}) = run {
    add("kapt", spec)?.apply { configure(this as T) }
    add(scope, spec)?.apply { configure(this as T) }
}
