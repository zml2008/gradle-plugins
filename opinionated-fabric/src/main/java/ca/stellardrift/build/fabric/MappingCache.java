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
package ca.stellardrift.build.fabric;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.lorenztiny.TinyMappingsJoiner;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import org.cadixdev.lorenz.MappingSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * An in-memory cache for mappings, to save time loading them.
 */
public abstract class MappingCache implements BuildService<BuildServiceParameters.None> {

    private final LoadingCache<MappingKey, MappingSet> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(15)
            .build(new CacheLoader<MappingKey, MappingSet>() {
                @Override
                public MappingSet load(final @NonNull MappingKey key) throws Exception {
                    return key.create();
                }
            });

    public MappingSet mappingsForMigration(final Project source, final File destination, final String environmentNamespace, final String matchingNamespace) {
        try {
            return this.cache.get(new MergedMigration(source, destination, environmentNamespace, matchingNamespace));
        } catch (final ExecutionException ex) {
            throw new GradleException("Failed to load mappings for migration", ex);
        }
    }

    interface MappingKey {
        @NonNull MappingSet create() throws IOException;

        int hashCode();
        boolean equals(final Object other);
    }

    static final class MergedMigration implements MappingKey {
        private final transient Gradle gradle;
        private final String sourcePath;
        private final File destination;
        private final String environmentNamespace;
        private final String matchingNamespace;

        /**
         *
         * @param source source project , will be used to query the source tiny tree
         * @param destination the destination mappings, as a zip file with mappings in the {@code mappings/mappings.tiny} file.
         * @param environmentNamespace namespace for the development environment
         * @param matchingNamespace namespace to map using
         */
        MergedMigration(final Project source, final File destination, final String environmentNamespace, final String matchingNamespace) {
            this.gradle = source.getGradle();
            this.sourcePath = source.getPath();
            this.destination = destination;
            this.environmentNamespace = environmentNamespace;
            this.matchingNamespace = matchingNamespace;
        }

        @Override
        public @NonNull MappingSet create() throws IOException {
            final TinyTree source = this.gradle.getRootProject().findProject(this.sourcePath).getExtensions().getByType(LoomGradleExtension.class).getMappingsProvider().getMappings();

            final TinyTree destination;
            try (final FileSystem jar = FileSystems.newFileSystem(this.destination.toPath(), (ClassLoader) null)) {
                final Path mappingsFile = jar.getPath("mappings/mappings.tiny");
                try (final BufferedReader reader = Files.newBufferedReader(mappingsFile, StandardCharsets.UTF_8)) {
                    destination = TinyMappingFactory.loadWithDetection(reader);
                }
            }

            return new TinyMappingsJoiner(
                    source, this.environmentNamespace,
                    destination, this.environmentNamespace,
                    this.matchingNamespace
            ).read();
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;
            MergedMigration that = (MergedMigration) other;
            return this.sourcePath.equals(that.sourcePath)
                    && this.destination.equals(that.destination)
                    && this.environmentNamespace.equals(that.environmentNamespace)
                    && this.matchingNamespace.equals(that.matchingNamespace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.sourcePath, this.destination, this.environmentNamespace, this.matchingNamespace);
        }
    }

}
