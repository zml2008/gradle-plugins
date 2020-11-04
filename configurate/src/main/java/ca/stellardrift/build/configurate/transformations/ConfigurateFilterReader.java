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

package ca.stellardrift.build.configurate.transformations;

import ca.stellardrift.build.configurate.ConfigSource;
import ca.stellardrift.build.configurate.ConfigTarget;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.Action;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

class ConfigurateFilterReader extends FilterReader {

    public static final String PARAM_SOURCE = "source";
    public static final String PARAM_DEST = "dest";
    public static final String PARAM_TRANSFORMER = "transformer";

    private final Reader original;

    // Configurate loaders operate on entire files
    // This isn't super efficient, but it's a start
    private @MonotonicNonNull ConfigSource source;
    private @MonotonicNonNull ConfigTarget dest;
    private @Nullable Action<ConfigurationNode> transformer;
    private boolean setUp = false;
    /**
     * Creates a new filtered reader.
     *
     * @param in a Reader object providing the underlying stream.
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     */
    ConfigurateFilterReader(final @NonNull Reader in) {
        super(in);
        this.original = in;
    }

    public void source(final ConfigSource source) {
        this.source = requireNonNull(source, "source");
    }

    public void dest(final ConfigTarget dest) {
        this.dest = requireNonNull(dest, "dest");
    }

    public void transformer(final Action<ConfigurationNode> func) {
        this.transformer = func;
    }

    private void transformIfSetUp() throws IOException {
        if (!this.setUp) { // only do it once
            if (this.source != null && this.dest != null) { // let's go!
                this.setUp = true;
                final ConfigurationNode node = this.source.read(this.in);
                if (this.transformer != null) {
                    this.transformer.execute(node);
                }
                final TrustedByteArrayOutput holder = new TrustedByteArrayOutput();
                try (final Writer writer = new OutputStreamWriter(holder, StandardCharsets.UTF_8)) {
                    this.dest.write(writer, node);
                }
                this.in = new InputStreamReader(holder.toInputStream(), StandardCharsets.UTF_8);
            }
        }
    }

    private void requireSetUp() throws IOException {
        if (!this.setUp) {
            transformIfSetUp();
            if (!this.setUp) {
                throw new IllegalStateException("Resource transformer has not received required source and dest loaders!");
            }
        }
    }

    // Overrides

    @Override
    public int read() throws IOException {
        requireSetUp();
        return super.read();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        requireSetUp();
        return super.read(cbuf, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        requireSetUp();
        return super.skip(n);
    }

    @Override
    public boolean ready() throws IOException {
        requireSetUp();
        return super.ready();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        requireSetUp();
        super.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        requireSetUp();
        super.reset();
    }

    @Override
    public void close() throws IOException {
        if (this.in != this.original) {
            this.original.close();
        }
        super.close();
    }

}
