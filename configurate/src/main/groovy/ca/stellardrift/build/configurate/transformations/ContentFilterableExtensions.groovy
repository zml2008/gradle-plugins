/*
 * Copyright 2020-2022 zml
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
package ca.stellardrift.build.configurate.transformations

import ca.stellardrift.build.configurate.ConfigProcessor
import ca.stellardrift.build.configurate.ConfigSource
import ca.stellardrift.build.configurate.ConfigTarget
import groovy.transform.CompileStatic
import org.gradle.api.file.ContentFilterable
import org.spongepowered.configurate.ConfigurationNode

/**
 * Extensions for {@link ContentFilterable}
 */
@CompileStatic
class ContentFilterableExtensions {

    private ContentFilterableExtensions() {
    }

    /**
     * Ensure that a configuration file can be successfully read by the specified format at build time.
     *
     * @param self receiver
     * @param format the format to validate against
     */
    static void validate(final ContentFilterable self, final ConfigSource format) {
        self.filter([(ConfigurateValidationReader.PARAM_FORMAT): format], ConfigurateValidationReader.class)
    }

    /**
     * Convert the format of files within {@code self}.
     *
     * @param self receiver
     * @param source format of the input file
     * @param target format of the output file
     * @param transformer an extra action that can be performed on a node. optional.
     */
    static void convertFormat(final ContentFilterable self, final ConfigSource source, final ConfigTarget target,
                              final @DelegatesTo(ConfigurationNode) Closure transformer = { }) {
        self.filter([
                (ConfigurateFilterReader.PARAM_SOURCE)     : source,
                (ConfigurateFilterReader.PARAM_DEST)       : target,
                (ConfigurateFilterReader.PARAM_TRANSFORMER): { it ->
                    transformer.delegate = it
                    transformer()
                }
        ], ConfigurateFilterReader.class)
    }

    /**
     * Perform a transformation on the deserialized node for a configuration.
     *
     * @param self receiver
     * @param configType for the type of a configuration file
     * @param transformer the action to perform on each node
     */
    static void transform(final ContentFilterable self, final ConfigProcessor<?, ?> configType,
                          final @DelegatesTo(ConfigurationNode) Closure transformer) {
        self.filter([
                (ConfigurateFilterReader.PARAM_SOURCE)     : configType,
                (ConfigurateFilterReader.PARAM_DEST)       : configType,
                (ConfigurateFilterReader.PARAM_TRANSFORMER): { it ->
                    transformer.delegate = it
                    transformer()
                }
        ], ConfigurateFilterReader.class)
    }

}
