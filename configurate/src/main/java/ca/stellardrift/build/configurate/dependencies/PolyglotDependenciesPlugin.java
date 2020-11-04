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

import ca.stellardrift.build.configurate.ConfigFormats;
import ca.stellardrift.build.configurate.ConfigProcessor;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.nio.file.Files;
import java.nio.file.Path;

public class PolyglotDependenciesPlugin implements Plugin<Settings> {
    private static final Logger LOGGER = Logging.getLogger(PolyglotDependenciesPlugin.class);
    private static final String DEPENDENCIES_FILE_NAME = "dependencies";
    @Override
    public void apply(final Settings target) {

        final PolyglotDependenciesExtension deps = target.getExtensions()
                .create("deps", PolyglotDependenciesExtension.class, target);

        // Register a listener to register the dependencies for the first available format
        target.getGradle().settingsEvaluated(settings -> {
            final Path gradleDir = settings.getRootDir().toPath().resolve("gradle");
            for (final ConfigProcessor<?, ?> processor : ConfigFormats.all()) {
                for (final String extension : processor.extensions()) {
                    final Path candidate = gradleDir.resolve(DEPENDENCIES_FILE_NAME + "." + extension);
                    if (Files.exists(candidate)) {
                        LOGGER.info("Chose file {} for dependencies manifest", candidate);
                        deps.from(processor.configured(build -> build.defaultOptions(opts -> opts.serializers(serial -> serial.register(GradleVersion.class, GradleVersion.Serializer.INSTANCE)))), candidate);
                        return;
                    }
                }
            }
        });
    }
}
