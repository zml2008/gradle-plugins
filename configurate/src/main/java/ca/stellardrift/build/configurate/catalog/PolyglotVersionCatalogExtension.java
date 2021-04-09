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
package ca.stellardrift.build.configurate.catalog;

import ca.stellardrift.build.configurate.ConfigSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Action;
import org.gradle.api.initialization.Settings;
import org.gradle.api.initialization.dsl.VersionCatalogBuilder;
import org.gradle.api.initialization.resolve.DependencyResolutionManagement;
import org.gradle.plugin.use.PluginDependenciesSpec;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Register dependencies manifests
 */
public abstract class PolyglotVersionCatalogExtension {
    private final Settings settings;

    public PolyglotVersionCatalogExtension(final Settings settings) {
        this.settings = settings;
    }

    /**
     * Populate the default version catalog with the information from {@code file}.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param source the source describing the config format to use
     * @param file the file to read
     */
    public void from(final ConfigSource source, final File file) {
        this.from(source, file.toPath());
    }

    /**
     * Populate the default version catalog with the information from {@code file}.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param source the source describing the config format to use
     * @param file the file to read
     */
    public void from(final ConfigSource source, final Path file) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(file, "file");

        this.populateCatalog(this.settings.getDependencyResolutionManagement().getDefaultLibrariesExtensionName().get(), source, file);
    }

    /**
     * Populate a version catalog builder.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param versionCatalog the name of the builder to populate
     * @param source the config source describing this version catalog
     * @param file the file to load
     */
    public void populateCatalog(final String versionCatalog, final ConfigSource source, final File file) {
       this.populateCatalog(versionCatalog, source, file.toPath());
    }

    /**
     * Populate a version catalog builder.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param versionCatalog the name of the builder to populate
     * @param source the config source describing this version catalog
     * @param file the file to load
     */
    public void populateCatalog(final String versionCatalog, final ConfigSource source, final Path file) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(file, "file");
        final PluginDependenciesSpec plugins = this.settings.getPluginManagement().getPlugins();
        final DependencyResolutionManagement drm = this.settings.getDependencyResolutionManagement();

        drm.getVersionCatalogs().register(versionCatalog, new VersionCatalogBuilderConfigurationAction(source, file, plugins));
    }

    /**
     * Populate a version catalog builder.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param versionCatalog the builder to populate
     * @param source the config source describing this version catalog
     * @param file the file to load
     */
    public void populateCatalog(final VersionCatalogBuilder versionCatalog, final ConfigSource source, final File file) {
        this.populateCatalog(versionCatalog, source, file.toPath());
    }

    /**
     * Populate a version catalog builder.
     *
     * <p>If {@code file} doesn't exist, this will be a no-op.</p>
     *
     * @param versionCatalog the builder to populate
     * @param source the config source describing this version catalog
     * @param file the file to load
     */
    public void populateCatalog(final VersionCatalogBuilder versionCatalog, final ConfigSource source, final Path file) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(file, "file");
        final PluginDependenciesSpec plugins = this.settings.getPluginManagement().getPlugins();
        new VersionCatalogBuilderConfigurationAction(source, file, plugins).execute(versionCatalog);
    }

    static class VersionCatalogBuilderConfigurationAction implements Action<VersionCatalogBuilder> {
        private final ConfigSource source;
        private final Path file;
        private final PluginDependenciesSpec plugins;

        VersionCatalogBuilderConfigurationAction(final ConfigSource source, final Path file, final PluginDependenciesSpec plugins) {
            this.source = source;
            this.file = file;
            this.plugins = plugins;
        }

        @Override
        public void execute(final @NonNull VersionCatalogBuilder builder) {
            final VersionCatalogApplier app = new VersionCatalogApplier(builder, this.plugins);
            try (final BufferedReader reader = Files.newBufferedReader(this.file, StandardCharsets.UTF_8)) {
                final ConfigurationNode node = this.source.read(
                    reader,
                    opts -> opts.serializers(serializers -> serializers.register(GradleVersion.class, GradleVersion.Serializer.INSTANCE))
                );
                app.load(node);
            } catch (final IOException ex) {
                throw new RuntimeException("Unable to read versions catalog", ex);
            }
        }
    }
}
