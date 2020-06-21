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

package ca.stellardrift.build.transformations

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterReader
import java.io.Reader
import java.io.Writer
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.gson.GsonConfigurationLoader
import ninja.leaping.configurate.hocon.HoconConfigurationLoader
import ninja.leaping.configurate.loader.AbstractConfigurationLoader
import ninja.leaping.configurate.xml.XMLConfigurationLoader
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ContentFilterable
import java.io.IOException

class ConfigurateTransformationsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // we mostly exist for adding to the project namespace, no need to apply anything really
        // maybe a project extension?
    }
}

/**
 * A provider for a configuration node, one half of a loader
 */
interface ConfigSource {
    fun read(source: Reader): ConfigurationNode
}

/**
 * A receiver of a configuration node, the other half of the loader
 */
interface ConfigTarget {
    fun write(destination: Writer, node: ConfigurationNode)
}

/**
 * An operator that can both read and write configurations
 */
interface ConfigProcessor : ConfigSource, ConfigTarget

// TODO: For configurate 4.0, the AbstractConfigurationLoader.Builder class will
//  be public so users can customize loader options
enum class ConfigFormats : ConfigProcessor {
    HOCON {
        override fun read(source: Reader): ConfigurationNode {
            val loader = HoconConfigurationLoader.builder()
                .setSource { BufferedReader(source) }
                .build()
            return loader.load()
        }

        override fun write(destination: Writer, node: ConfigurationNode) {
            val loader = HoconConfigurationLoader.builder()
                .setSink { BufferedWriter(destination) }
                .build()
            loader.save(node)
        }
    },
    GSON {
        override fun read(source: Reader): ConfigurationNode {
            val loader = GsonConfigurationLoader.builder()
                .setSource { BufferedReader(source) }
                .build()
            return loader.load()
        }

        override fun write(destination: Writer, node: ConfigurationNode) {
            val loader = GsonConfigurationLoader.builder()
                .setSink { BufferedWriter(destination) }
                .build()
            loader.save(node)
        }
    },
    YAML {
        override fun read(source: Reader): ConfigurationNode {
            val loader = YAMLConfigurationLoader.builder()
                .setSource { BufferedReader(source) }
                .build()
            return loader.load()
        }

        override fun write(destination: Writer, node: ConfigurationNode) {
            val loader = YAMLConfigurationLoader.builder()
                .setSink { BufferedWriter(destination) }
                .build()
            loader.save(node)
        }
    },
    XML {
        override fun read(source: Reader): ConfigurationNode {
            val loader = XMLConfigurationLoader.builder()
                .setSource { BufferedReader(source) }
                .build()
            return loader.load()
        }

        override fun write(destination: Writer, node: ConfigurationNode) {
            val loader = XMLConfigurationLoader.builder()
                .setSink { BufferedWriter(destination) }
                .build()
            loader.save(node)
        }
    }
}

/**
 * Convert any file targeted from the [source] format to the [dest] format.
 *
 * Conversion doesn't process file extensions, so most files will want to be renamed as part of the conversion process.
 */
fun ContentFilterable.convertFormat(source: ConfigSource, dest: ConfigTarget, transformer: (ConfigurationNode) -> Unit = {}) {
    this.filter(mapOf("source" to source, "dest" to dest, "transformer" to transformer), ConfigurateFilterReader::class.java)
}

/**
 * Load every file to be processed
 */
fun ContentFilterable.transform(configType: ConfigProcessor, transformer: (ConfigurationNode) -> Unit) {
    this.filter(mapOf("source" to configType, "dest" to configType, "transformer" to transformer), ConfigurateFilterReader::class.java)
}

internal class ConfigurateFilterReader(private val originalIn: Reader) : FilterReader(originalIn) {
    // Configurate loaders operate on entire files
    // This isn't super efficient, but it's a start
    private lateinit var _source: ConfigSource
    private lateinit var _dest: ConfigTarget
    private lateinit var _transformer: (ConfigurationNode) -> Unit
    private var setUp = false
    private var cachedLoadError: IOException? = null

    fun source(loader: ConfigSource) {
        this._source = loader
        trySetUp()
    }

    fun dest(loader: ConfigTarget) {
        this._dest = loader
        trySetUp()
    }

    fun transformer(func: (ConfigurationNode) -> Unit) {
        this._transformer = func
        trySetUp()
    }

    private fun trySetUp() {
        if (!this.setUp) { // only do it once
            if (this::_source.isInitialized && this::_dest.isInitialized && this::_transformer.isInitialized) { // let's go!
                this.setUp = true
                try {
                    val node = this._source.read(this.`in`)
                    this._transformer(node)
                    val holder = TrustedByteArrayOutput()
                    this._dest.write(holder.writer(), node)
                    this.`in` = ByteArrayInputStream(holder.rawArray).bufferedReader()
                } catch (ex: IOException) {
                    this.cachedLoadError = ex
                }
            }
        }
    }

    private fun requireSetUp() {
        if (!this.setUp) {
            throw IllegalStateException("Resource transformer has not received required source and dest loaders!")
        }
        val cachedLoadError = this.cachedLoadError
        if (cachedLoadError != null) {
            throw cachedLoadError
        }
    }

    override fun read(cbuf: CharArray): Int {
        requireSetUp()
        return super.read(cbuf)
    }

    override fun read(): Int {
        requireSetUp()
        return super.read()
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        requireSetUp()
        return super.read(cbuf, off, len)
    }

    override fun skip(n: Long): Long {
        requireSetUp()
        return super.skip(n)
    }

    override fun ready(): Boolean {
        requireSetUp()
        return super.ready()
    }

    override fun reset() {
        requireSetUp()
        super.reset()
    }

    override fun markSupported(): Boolean {
        requireSetUp()
        return super.markSupported()
    }

    override fun mark(readAheadLimit: Int) {
        requireSetUp()
        super.mark(readAheadLimit)
    }

    override fun close() {
        if (this.`in` !== this.originalIn) {
            this.originalIn.close()
        }
        super.close()
    }
}

/**
 * A [ByteArrayOutputStream] that directly exposes its buffer,
 * so we can use a [ByteArrayInputStream] without requiring a copy
 */
private class TrustedByteArrayOutput : ByteArrayOutputStream() {
    val rawArray: ByteArray get() = this.buf
}
