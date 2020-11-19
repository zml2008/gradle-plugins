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

package ca.stellardrift.build.kotlin

import ca.stellardrift.build.common.OpinionatedDefaultsPlugin
import net.kyori.indra.extension
import net.kyori.indra.versionString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class OpinionatedKotlinDefaultsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(OpinionatedDefaultsPlugin::class.java)
            plugins.apply("kotlin")

            dependencies.apply {
                add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }

            val indraExtension = extension(this)
            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=enable")
            }

            afterEvaluate {
                tasks.withType(KotlinCompile::class.java).configureEach {
                    it.kotlinOptions.jvmTarget = versionString(indraExtension.javaVersions.target.get())
                }
            }
        }
    }
}
