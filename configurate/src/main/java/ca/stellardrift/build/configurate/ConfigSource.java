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

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;

import java.io.Reader;
import java.util.function.UnaryOperator;

/**
 * A provider for a configuration node, one half of a loader
 */
public interface ConfigSource {

    /**
     * Read a node from the provided reader.
     *
     * @param reader the reader to use
     * @return a loaded node
     * @throws ConfigurateException if any error occurs while loading
     */
    default ConfigurationNode read(final Reader reader) throws ConfigurateException {
        return this.read(reader, UnaryOperator.identity());
    }


    /**
     * Read a node from the provided reader.
     *
     * @param reader the reader to use
     * @param optionsConfiguration an operator to tweak the options used in this source
     * @return a loaded node
     * @throws ConfigurateException if any error occurs while loading
     */
    ConfigurationNode read(final Reader reader, final UnaryOperator<ConfigurationOptions> optionsConfiguration) throws ConfigurateException;
}
