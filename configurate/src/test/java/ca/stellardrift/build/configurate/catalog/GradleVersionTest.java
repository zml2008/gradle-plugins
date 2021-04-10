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
package ca.stellardrift.build.configurate.catalog;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedConsumer;
import org.spongepowered.configurate.util.UnmodifiableCollections;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradleVersionTest {

    private static final ConfigurationOptions OPTIONS = ConfigurationOptions.defaults()
            .nativeTypes(UnmodifiableCollections.toSet(String.class, Map.class, List.class, Integer.class))
            .serializers(ser -> ser.register(GradleVersion.class, GradleVersion.Serializer.INSTANCE));

    private GradleVersion read(final CheckedConsumer<ConfigurationNode, SerializationException> builder) throws SerializationException {
        return BasicConfigurationNode.root(OPTIONS, builder).get(GradleVersion.class);
    }

    private ConfigurationNode write(final GradleVersion version) throws SerializationException {
        final ConfigurationNode node = BasicConfigurationNode.root(OPTIONS);
        node.set(version);
        return node;
    }

    @Test
    void testStrictVersion() throws SerializationException {
        final GradleVersion version = this.read(n -> n.set("1.2.3!!"));

        // read
        assertEquals(GradleVersion.builder().strictly("1.2.3").build(), version);

        // write
        assertEquals("1.2.3!!", this.write(version).getString());
    }

    @Test
    void testStrictAndPreferredVersion() throws SerializationException {
        final GradleVersion version = this.read(n -> n.set("[1.7, 1.8[!!1.7.25"));
        // read
        assertEquals(GradleVersion.builder().strictly("[1.7, 1.8[").prefer("1.7.25").build(), version);

        // write
        assertEquals("[1.7, 1.8[!!1.7.25", this.write(version).getString());
    }

    @Test
    void testRequire() throws SerializationException {
        final GradleVersion version = this.read(n -> n.set("8.41.1"));

        // read
        assertEquals(GradleVersion.builder().require("8.41.1").build(), version);

        // write
        assertEquals("8.41.1", this.write(version).getString());
    }
}
