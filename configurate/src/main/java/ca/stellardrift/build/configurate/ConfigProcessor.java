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
package ca.stellardrift.build.configurate;

import static java.util.Objects.requireNonNull;

import org.gradle.api.Action;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * An operator that can both read and write configurations.
 *
 * @param <B> builder type
 * @param <L> loader type
 */
public final class ConfigProcessor<B extends AbstractConfigurationLoader.Builder<B, L>, L extends AbstractConfigurationLoader<?>> implements ConfigSource, ConfigTarget {
    private final Supplier<B> builderMaker;
    private final Set<String> extensions;

    ConfigProcessor(final Supplier<B> builderMaker, final String... extensions) {
        this.builderMaker = requireNonNull(builderMaker);
        this.extensions = UnmodifiableCollections.toSet(extensions);
    }

    private ConfigProcessor(final Supplier<B> builderMaker, final Set<String> extensions) {
        this.builderMaker = requireNonNull(builderMaker);
        this.extensions = extensions;
    }

    @Override
    public ConfigurationNode read(final Reader reader) throws ConfigurateException {
        requireNonNull(reader, "reader");
        final ConfigurationLoader<?> loader = this.builderMaker.get()
                .source(() -> new BufferedReader(reader))
                .build();
        return loader.load();
    }

    @Override
    public ConfigurationNode read(final Reader reader, final UnaryOperator<ConfigurationOptions> optionsModifier) throws ConfigurateException {
        requireNonNull(reader, "reader");
        final ConfigurationLoader<?> loader = this.builderMaker.get()
                .defaultOptions(optionsModifier)
                .source(() -> new BufferedReader(reader))
                .build();
        return loader.load();
    }

    @Override
    public void write(final Writer destination, final ConfigurationNode node) throws ConfigurateException {
        requireNonNull(destination, "destination");
        requireNonNull(node, "node");
        final ConfigurationLoader<?> loader = this.builderMaker.get()
                .sink(() -> new BufferedWriter(destination))
                .build();
        loader.save(node);
    }

    /**
     * Create a derived configuration format that applies additional configuration to this format's builder.
     *
     * @param builderModifier The builder modifier
     * @return a derived format
     */
    public ConfigProcessor<B, L> configured(final Action<B> builderModifier) {
        requireNonNull(builderModifier, "builderModifier");
        return new ConfigProcessor<>(() -> {
            final B ret = this.builderMaker.get();
            builderModifier.execute(ret);
            return ret;
        }, this.extensions);
    }

    /**
     * Get extensions known to be supported by this configuration format.
     *
     * @return supported extensions
     */
    public Set<String> extensions() {
        return this.extensions;
    }
}
