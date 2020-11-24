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
package ca.stellardrift.build.configurate.transformations;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * A plugin that applies transformations using configurate.
 *
 * The plugin itself doesn't do anything, it's just a tool to get utility functions onto the buildscript classpath.
 */
public class ConfigurateTransformationsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

    }
}
