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
package ca.stellardrift.build.fabric

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

class OpinionatedFabricPluginTest {
    @Test
    fun `plugin applies successfully`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("ca.stellardrift.opinionated.fabric")

        // Verify the result
        assertNotNull(project.tasks.findByName("testmodJar"))
    }
}
