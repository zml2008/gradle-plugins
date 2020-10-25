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

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterReader
import java.io.IOException
import java.io.Reader
import java.io.Writer
import java.nio.charset.StandardCharsets
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ContentFilterable
import org.gradle.kotlin.dsl.invoke
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.xml.XmlConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

class ConfigurateTransformationsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // TODO: Figure out how to register our extension functions for Groovy classes
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

enum class ConfigFormats(private val maker: () -> AbstractConfigurationLoader.Builder<*, *>) : ConfigProcessor {
    HOCON(HoconConfigurationLoader::builder),
    GSON(GsonConfigurationLoader::builder),
    YAML(YamlConfigurationLoader::builder),
    XML(XmlConfigurationLoader::builder);

    override fun read(source: Reader): ConfigurationNode {
        val loader = this.maker()
                .source { BufferedReader(source) }
                .build()
        return loader.load()
    }

    override fun write(destination: Writer, node: ConfigurationNode) {
        val loader = this.maker()
                .sink { BufferedWriter(destination) }
                .build()
        loader.save(node)
    }
}

/**
 * Ensure that a configuration file can be successfully read by the specified format at build time
 */
fun ContentFilterable.validate(format: ConfigSource) {
    this.filter(mapOf("format" to format), ConfigurateValidationReader::class.java)
}

/**
 * Convert any file targeted from the [source] format to the [dest] format.
 *
 * Conversion doesn't process file extensions, so most files will want to be renamed as part of the conversion process.
 */
@JvmOverloads
fun ContentFilterable.convertFormat(source: ConfigSource, dest: ConfigTarget, transformer: Action<ConfigurationNode>? = null) {
    this.filter(mapOf("source" to source, "dest" to dest, "transformer" to transformer), ConfigurateFilterReader::class.java)
}

/**
 * Load every file to be processed
 */
fun ContentFilterable.transform(configType: ConfigProcessor, transformer: Action<ConfigurationNode>) {
    this.filter(mapOf("source" to configType, "dest" to configType, "transformer" to transformer), ConfigurateFilterReader::class.java)
}

internal class ConfigurateFilterReader(private val originalIn: Reader) : FilterReader(originalIn) {
    // Configurate loaders operate on entire files
    // This isn't super efficient, but it's a start
    private lateinit var _source: ConfigSource
    private lateinit var _dest: ConfigTarget
    private var _transformer: Action<ConfigurationNode>? = null
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

    fun transformer(func: Action<ConfigurationNode>?) {
        this._transformer = func
        trySetUp()
    }

    private fun trySetUp() {
        if (!this.setUp) { // only do it once
            if (this::_source.isInitialized && this::_dest.isInitialized) { // let's go!
                this.setUp = true
                try {
                    val node = this._source.read(this.`in`)
                    this._transformer?.invoke(node)
                    val holder = TrustedByteArrayOutput()
                    this._dest.write(holder.writer(), node)
                    this.`in` = holder.toInputStream().bufferedReader()
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
    fun toInputStream() = ByteArrayInputStream(this.buf, 0, count)
}

class ConfigurateValidationReader(private val original: Reader) : FilterReader(original) {
    // when configured, read, validate, and set in to new ByteArrayStream
    fun format(format: ConfigSource) {
        val output = TrustedByteArrayOutput()
        output.writer(StandardCharsets.UTF_8).use {
            val buffer = CharArray(2048)
            var read = original.read(buffer)
            while (read != -1) {
                it.write(buffer, 0, read)
                read = original.read(buffer)
            }
        }
        // Will throw exception on failure
        format.read(output.toInputStream().reader())

        // And go back to an input stream from the original
        this.`in` = output.toInputStream().reader()
    }
}
