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
package ca.stellardrift.build.configurate.dependencies;

import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;


public class PolyglotDependenciesPluginTest {

    @Test
    public void testEmptyBuild() {
        // TODO: How can we test Settings plugins?

        /*final Project project = ProjectBuilder.builder()
                .withName("widget-party")
                .build();
        project.getPluginManager().apply("ca.stellardrift.opinionated");

        assertNotNull(project.getExtensions().findByName("opinionated"));*/
    }
}
