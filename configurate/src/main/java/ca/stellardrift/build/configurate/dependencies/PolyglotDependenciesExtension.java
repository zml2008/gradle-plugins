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

import ca.stellardrift.build.configurate.ConfigSource;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.dsl.VersionCatalogBuilder;
import org.gradle.plugin.use.PluginDependenciesSpec;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class PolyglotDependenciesExtension {
    private final Settings settings;

    public PolyglotDependenciesExtension(final Settings settings) {
        this.settings = settings;
    }

    public void from(final ConfigSource source, final File file) {
        this.from(source, file.toPath());
    }

    public void from(final ConfigSource source, final Path file) {
        final PluginDependenciesSpec plugins = settings.getPluginManagement().getPlugins();
        settings.dependencyResolutionManagement(drm -> {
            final String libs = drm.getDefaultLibrariesExtensionName().get();
            drm.versionCatalogs(catalogs -> {
                final VersionCatalogBuilder builder = catalogs.maybeCreate(libs);
                final VersionCatalogApplier app = new VersionCatalogApplier(builder, plugins);
                try (final BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    final ConfigurationNode node = source.read(reader);
                    app.load(node);
                } catch (final IOException ex) {
                    throw new RuntimeException("Unable to read versions catalog", ex);
                }
            });
        });

    }
}
