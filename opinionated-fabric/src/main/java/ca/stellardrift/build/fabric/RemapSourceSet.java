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

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.remapper.MercuryRemapper;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RemapSourceSet extends DefaultTask {

    // Properties

    @InputFiles
    public abstract ConfigurableFileCollection getSourceDirs();

    @CompileClasspath
    public abstract ConfigurableFileCollection getClasspath();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @InputFile
    public abstract RegularFileProperty getMappings();

    @OutputFile
    public abstract RegularFileProperty getRemapLocations();

    @Input
    public abstract Property<String> getEnvironmentNamespace();

    @Input
    public abstract Property<String> getMatchingNamespace();

    @Input
    public abstract Property<String> getSourceCompatibility();

    // Necessary services

    @Internal
    protected abstract Property<MappingCache> getMappingCache();

    public RemapSourceSet() {
        this.setGroup("stellardrift");
        this.getEnvironmentNamespace().convention("named");
        this.getMatchingNamespace().convention("intermediary");
    }

    @TaskAction
    public void doRemap() throws IOException {
        final MappingSet mappings = this.getMappingCache().get().mappingsForMigration(
                this.getProject(),
                this.getMappings().get().getAsFile(),
                this.getEnvironmentNamespace().get(),
                this.getMatchingNamespace().get()
        );

        final Mercury mercury = new Mercury();
        // Setup processors (TODO: make this configurable?)
        mercury.getProcessors().add(MercuryRemapper.create(mappings));
        mercury.setGracefulClasspathChecks(true);
        mercury.setGracefulJavadocClasspathChecks(true);
        mercury.setSourceCompatibility(this.getSourceCompatibility().get());

        // Set up classpath
        mercury.getClassPath().addAll(this.getClasspath()
                .filter(File::exists)
                .getFiles().stream()
                .map(File::toPath)
                .collect(Collectors.toSet()));

        // Run processing
        this.getLogger().info("Running remap for {}", this.getName());

        final Map<Path, Path> remapLocations = new HashMap<>();
        final Set<File> sourceDirs = this.getSourceDirs().getFiles();
        final Path destinationBase = this.getOutputDirectory().get().getAsFile().toPath();
        Files.walkFileTree(destinationBase, new FileVisitor<Path>() {
            // @formatter:off
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) { return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult visitFileFailed(Path file, IOException exc) { return FileVisitResult.CONTINUE; }
            // @formatter:on

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        for (final File sourceFile : sourceDirs) {
            final Path source = sourceFile.toPath();
            Path dest = destinationBase.resolve(source.getFileName());
            while (Files.exists(dest)) {
                dest = dest.resolveSibling(dest.getFileName().toString() + "_");
            }

            if (sourceFile.exists()) {
                remapLocations.put(source, dest);
                Files.createDirectories(dest);
                this.getLogger().warn("Migrating mappings from {} to {}", source, dest);
                try {
                    mercury.rewrite(source, dest);
                } catch (final Exception ex) {
                    throw new GradleException("Failed to remap source directory " + source, ex);
                }
            }
        }

        // Then write out remap locations
        if (this.getRemapLocations().isPresent()) {
            final File remapLocationsOutput = this.getRemapLocations().get().getAsFile();
            remapLocationsOutput.getParentFile().mkdirs();
            try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(remapLocationsOutput), StandardCharsets.UTF_8))) {
                for (final Map.Entry<Path, Path> entry : remapLocations.entrySet()) {
                    writer.write(entry.getKey().toAbsolutePath() + "\t" + entry.getValue().toAbsolutePath());
                    writer.newLine();
                }
            }
        }
    }
}
