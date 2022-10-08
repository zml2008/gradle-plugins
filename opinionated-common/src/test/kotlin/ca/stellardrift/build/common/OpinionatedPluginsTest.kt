/*
 * Copyright 2020-2022 zml
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

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.gradle.testfixtures.ProjectBuilder

/**
 * Basic tests to make sure each plugin applies properly
 */
class OpinionatedPluginsTest {

    @Test
    @Ignore("ProjectBuilder projects do not have the right services for Spotless")
    fun `opinionated defaults plugin test`() {
        val project = ProjectBuilder.builder()
            .withName("widget-party")
            .build()
        project.plugins.apply("ca.stellardrift.opinionated")

        assertNotNull(project.extensions.findByName("opinionated"))
    }
}
