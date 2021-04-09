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

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class MigrateMappingsInPlace extends DefaultTask {

    /**
     * Collection of files with lines which are each:
     * &lt;original&gt;\t&lt;remapped&gt;
     * in absolute paths
     *
     * @return directory mappings
     */
    @InputFiles
    public abstract ConfigurableFileCollection getDirectoryMappings();

    @Inject
    protected abstract FileSystemOperations getFileOps();

    public MigrateMappingsInPlace() {
        this.setGroup("stellardrift");
    }

    @TaskAction
    public void restoreInPlace() {
        // Load mappings from temporary file
        final Map<File, File> toMove = new HashMap<>();
        for (final File it : this.getDirectoryMappings().getFiles()) {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(it), StandardCharsets.UTF_8))) {
                reader.lines()
                        .filter(line -> !line.isEmpty())
                        .forEach(line -> {
                            this.getLogger().info("Found mapping: {}", line);
                            final String[] pair = line.split("\t", 2);
                            this.getLogger().info("Will move {} to {}", pair[1], pair[0]);
                            toMove.put(new File(pair[1]), new File(pair[0]));
                        });
            } catch (final IOException ex) {
                throw new GradleException("Failed to read directory mapping file " + it, ex);
            }
        }

        // Delete target directories
        this.getFileOps().delete(spec -> spec.delete(toMove.values().toArray(new Object[0])));

        // Then move them back
        for (final Map.Entry<File, File> entry : toMove.entrySet()) {
            getFileOps().copy(spec -> {
                spec.from(entry.getKey());
                spec.into(entry.getValue());
            });
        }

    }
}
