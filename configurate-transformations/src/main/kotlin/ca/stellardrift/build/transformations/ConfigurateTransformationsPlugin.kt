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

@file:JvmName("ConfigurateTransformations")
package ca.stellardrift.build.transformations

import org.gradle.api.Action
import org.gradle.api.file.ContentFilterable
import org.spongepowered.configurate.ConfigurationNode

/**
 * Ensure that a configuration file can be successfully read by the specified format at build time
 */
fun ContentFilterable.validate(format: ConfigSource) {
    this.filter(mapOf(ConfigurateValidationReader.PARAM_FORMAT to format), ConfigurateValidationReader::class.java)
}

/**
 * Convert any file targeted from the [source] format to the [dest] format.
 *
 * Conversion doesn't process file extensions, so most files will want to be renamed as part of the conversion process.
 */
@JvmOverloads
fun ContentFilterable.convertFormat(source: ConfigSource, dest: ConfigTarget, transformer: Action<ConfigurationNode>? = null) {
    this.filter(mapOf(
        ConfigurateFilterReader.PARAM_SOURCE to source,
        ConfigurateFilterReader.PARAM_DEST to dest,
        ConfigurateFilterReader.PARAM_TRANSFORMER to transformer
    ), ConfigurateFilterReader::class.java)
}

/**
 * Load every file to be processed
 */
fun ContentFilterable.transform(configType: ConfigProcessor, transformer: Action<ConfigurationNode>) {
    this.filter(mapOf(
            ConfigurateFilterReader.PARAM_SOURCE to configType,
            ConfigurateFilterReader.PARAM_DEST to configType,
            ConfigurateFilterReader.PARAM_TRANSFORMER to transformer
    ), ConfigurateFilterReader::class.java)
}
