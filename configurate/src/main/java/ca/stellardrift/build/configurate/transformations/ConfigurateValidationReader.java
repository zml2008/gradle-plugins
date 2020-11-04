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
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;

class ConfigurateValidationReader extends FilterReader {

    public static final String PARAM_FORMAT = "format";

    private final Reader original;

    /**
     * Creates a new filtered reader.
     *
     * @param in a Reader object providing the underlying stream.
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     */
    protected ConfigurateValidationReader(@NotNull Reader in) {
        super(in);
        this.original = in;
    }


    // when configured, read, validate, and set in to new ByteArrayStream
    public void format(final ConfigSource format) throws IOException {
        final TrustedByteArrayOutput output = new TrustedByteArrayOutput();
        try (final OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            final char[] buffer = new char[2048];
            int read = this.original.read(buffer);
            while (read != -1) {
                writer.write(buffer, 0, read);
                read = original.read(buffer);
            }
        }

        // Will throw exception on failure
        try (final Reader reader = new InputStreamReader(output.toInputStream(), StandardCharsets.UTF_8)) {
            format.read(reader);
        }

        // And go back to an input stream from the original
        this.in = new InputStreamReader(output.toInputStream());
    }
}
