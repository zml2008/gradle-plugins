/*
 * Copyright 2022 zml
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
package ca.stellardrift.build.repository;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;


public class StellardriftRepositoryPluginTest {
    private static final String ID = "ca.stellardrift.repository";

    @Test
    void testPluginApplies() {
        final Project project = ProjectBuilder.builder()
                .withName("widget-party")
                .build();
        project.getPlugins().apply(ID);
    }
}
