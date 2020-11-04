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

package ca.stellardrift.build.transformations;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Standard formats for configuration
 */
public enum ConfigFormats implements ConfigProcessor {
    HOCON(HoconConfigurationLoader::builder),
    GSON(GsonConfigurationLoader::builder),
    YAML(YamlConfigurationLoader::builder),
    XML(XmlConfigurationLoader::builder);
    ;

    private final Supplier<AbstractConfigurationLoader.Builder<?, ?>> builderMaker;

    ConfigFormats(final Supplier<AbstractConfigurationLoader.Builder<?, ?>> builderMaker) {
        this.builderMaker = requireNonNull(builderMaker);
    }

    @Override
    public ConfigurationNode read(Reader reader) throws ConfigurateException {
        final ConfigurationLoader<?> loader = this.builderMaker.get()
                .source(() -> new BufferedReader(reader))
                .build();
        return loader.load();
    }

    @Override
    public void write(Writer destination, ConfigurationNode node) throws ConfigurateException {
        final ConfigurationLoader<?> loader = this.builderMaker.get()
                .sink(() -> new BufferedWriter(destination))
                .build();
        loader.save(node);
    }
}
