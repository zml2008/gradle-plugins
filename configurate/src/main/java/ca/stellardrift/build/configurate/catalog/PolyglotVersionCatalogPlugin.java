/*
 * Copyright 2020-2023 zml
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
package ca.stellardrift.build.configurate.catalog;

import ca.stellardrift.build.configurate.ConfigFormats;
import ca.stellardrift.build.configurate.ConfigProcessor;
import ca.stellardrift.build.configurate.GradleVersionUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.util.GradleVersion;

public class PolyglotVersionCatalogPlugin implements Plugin<Settings> {

    /**
     * The current version catalog format version.
     */
    public static final String FORMAT_VERSION;

    static {
        // prevent version from being inlined
        FORMAT_VERSION = "1.0";
    }

    private static final Logger LOGGER = Logging.getLogger(PolyglotVersionCatalogPlugin.class);
    private static final String DEPENDENCIES_FILE_NAME = "libs.versions";

    @Override
    public void apply(final Settings target) {
        final PolyglotVersionCatalogExtension deps = target.getExtensions()
                .create("deps", PolyglotVersionCatalogExtension.class, target);

        if (!GradleVersionUtil.VERSION_CATALOGS_STABLE) {
            throw new GradleException("The polyglot version catalog plugin no longer supports incubating versions of version catalogs.\n" +
                "Please use Gradle 7.4 or newer (you currently have " + GradleVersion.current() + ")");
        }

        // Register a listener to register the dependencies for the first available format found, after evaluating the Settings
        target.getGradle().settingsEvaluated(settings -> {
            final Path gradleDir = settings.getRootDir().toPath().resolve("gradle");
            for (final ConfigProcessor<?, ?> processor : ConfigFormats.all()) {
                for (final String extension : processor.extensions()) {
                    final Path candidate = gradleDir.resolve(DEPENDENCIES_FILE_NAME + "." + extension);
                    if (Files.exists(candidate)) {
                        LOGGER.info("Chose file {} for dependencies manifest", candidate);
                        deps.from(processor, candidate);
                        return;
                    }
                }
            }
        });
    }
}
