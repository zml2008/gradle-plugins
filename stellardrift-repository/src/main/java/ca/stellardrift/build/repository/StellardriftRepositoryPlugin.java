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
package ca.stellardrift.build.repository;

import net.kyori.mammoth.ProjectOrSettingsPlugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.util.GradleVersion;
import org.jetbrains.annotations.Nullable;

public class StellardriftRepositoryPlugin implements ProjectOrSettingsPlugin {
    private static final GradleVersion MINIMUM_VERSION = GradleVersion.version("7.4");

    @Override
    public void applyToSettings(final Settings settings) {
        this.registerExtension(settings.getDependencyResolutionManagement().getRepositories());
    }

    @Override
    public void applyToProject(final Project project) {
        this.registerExtension(project.getRepositories());
    }

    private void registerExtension(final RepositoryHandler handler) {
        ((ExtensionAware) handler).getExtensions().create(
            StellardriftRepositoryExtension.class,
            "stellardrift",
            StellardriftRepositoryExtensionImpl.class,
            handler
        );
    }

    @Override
    public @Nullable GradleVersion minimumGradleVersion() {
        return MINIMUM_VERSION;
    }
}
