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
package ca.stellardrift.build.configurate.dependencies;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.immutables.value.Value;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

@Value.Immutable
@Value.Style(
        get = "*",
        jdkOnly = true,
        overshadowImplementation = true,
        visibility = Value.Style.ImplementationVisibility.PACKAGE,
        defaultAsDefault = true)
interface GradleVersion {
    @Nullable String versionRef();
    @Nullable String require();
    @Nullable String strictly();
    @Nullable String prefer();
    List<String> rejectedVersions();
    default boolean rejectAll() {
        return false;
    }

    default void applyTo(final MutableVersionConstraint spec) {
        if (this.require() != null) {
            spec.require(this.require());
        }

        if (this.strictly() != null) {
            spec.strictly(this.strictly());
        }

        if (this.prefer() != null) {
            spec.prefer(this.prefer());
        }

        if (!this.rejectedVersions().isEmpty()) {
            spec.reject(this.rejectedVersions().toArray(new String[0]));
        }

        if (this.rejectAll()) {
            spec.rejectAll();
        }
    }

    /**
     * Get if this is a version that can't be expressed as a scalar string
     *
     * @return if complex version
     */
    default boolean complex() {
        return !this.rejectedVersions().isEmpty() || this.versionRef() != null || this.rejectAll() || this.require() != null;
    }

    class Builder extends ImmutableGradleVersion.Builder {
    }

    static class Serializer implements TypeSerializer<GradleVersion> {
        public static final Serializer INSTANCE = new Serializer();

        /**
         * Delimiter for rich version metadata.
         */
        private static final String RICH_DELIMITER = "!!";

        private static final String VERSION_REF = "ref";
        private static final String REQUIRE = "require";
        private static final String PREFER = "prefer";
        private static final String STRICTLY = "strictly";
        private static final String REJECT = "reject";
        private static final String REJECT_ALL = "rejectAll";

        private Serializer() {
        }

        @Override
        public GradleVersion deserialize(Type type, ConfigurationNode node) throws SerializationException {
            if (node.isList()) {
                throw new SerializationException("A version specification must be as either a String or a mapping of parameters");
            }
            final Builder builder = new Builder();
            if (node.isMap()) {
                // Complex metadata
                builder.versionRef(node.node(VERSION_REF).getString())
                .require(node.node(REQUIRE).getString())
                .prefer(node.node(PREFER).getString())
                .strictly(node.node(STRICTLY).getString())
                .addAllRejectedVersions(node.node(REJECT).getList(String.class, Collections.emptyList()))
                .rejectAll(node.node(REJECT_ALL).getBoolean(false));
            } else {
                parsePlain(node.getString(), builder);
            }

            return builder.build();
        }

        private void parsePlain(final String strictSpec, final Builder builder) throws SerializationException {
            final int delimiter = strictSpec.indexOf(RICH_DELIMITER);
            if (delimiter == 0) {
                 // invalid
            } else if (delimiter == -1) {
                builder.require(strictSpec);
            }
        }

        @Override
        public void serialize(Type type, @Nullable GradleVersion version, ConfigurationNode node) throws SerializationException {
            if (version == null) {
                node.raw(null);
                return;
            }

           if (node.isMap() || version.complex()) { // We're already a map, or we have data that can't be expressed as a scalar
           } else {

           }

        }


    }
}
