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

import io.leangen.geantyref.TypeFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.initialization.dsl.VersionCatalogBuilder;
import org.gradle.plugin.use.PluginDependenciesSpec;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Applies data from a configuration node to the Gradle model.
 */
final class VersionCatalogApplier {

    private final VersionCatalogBuilder builder;
    private final PluginDependenciesSpec plugins;

    private static final String METADATA = "metadata";
    private static final String DEPENDENCIES = "dependencies";
    private static final String BUNDLES = "bundles";
    private static final String PLUGINS = "plugins";
    private static final String VERSIONS = "versions";

    private static final Type MAP_STRING_STRING = TypeFactory.parameterizedClass(Map.class, String.class, String.class);
    private static final Type MAP_STRING_LIST_STRING = TypeFactory.parameterizedClass(Map.class, String.class, TypeFactory.parameterizedClass(List.class, String.class));
    private static final Type MAP_STRING_GRADLEVERSION = TypeFactory.parameterizedClass(Map.class, String.class, GradleVersion.class);

    public VersionCatalogApplier(final VersionCatalogBuilder builder, final PluginDependenciesSpec plugins) {
        this.builder = builder;
        this.plugins = plugins;
    }

    /**
     * Load a node into the Gradle dependencies model.
     *
     * @param node node to load
     * @throws SerializationException if information is provided in an invalid format
     */
    public void load(final ConfigurationNode node) throws SerializationException {
        final ConfigurationNode metadata = node.node(METADATA);
        if (!metadata.empty()) {
            this.metadata(metadata);
        }

        final ConfigurationNode dependencies = node.node(DEPENDENCIES);
        if (!dependencies.empty()) {
            this.dependencies(dependencies);
        }
        final ConfigurationNode bundles = node.node(BUNDLES);
        if (!bundles.empty()) {
            this.bundles(bundles);
        }

        // Plugins have been removed from version catalogs, todo: re-add when Gradle does
        /* final ConfigurationNode plugins = node.node(PLUGINS);
        if (!plugins.empty()) {
            this.plugins(plugins);
        } */

        final ConfigurationNode versions = node.node(VERSIONS);
        if (!versions.empty()) {
            this.versions(versions);
        }
    }

    private void metadata(final ConfigurationNode metadata) throws SerializationException {
       final String formatVersion = metadata.node("format", "version").getString();
       if (formatVersion != null && !formatVersion.equals(PolyglotVersionCatalogPlugin.FORMAT_VERSION)) {
           throw new SerializationException(metadata.parent(), VersionCatalogBuilder.class,
               "A version catalog was provided with format version " + formatVersion
                   + " but the polyglot catalog plugin only understands version " + PolyglotVersionCatalogPlugin.FORMAT_VERSION);
       }
    }

    // A mapping of key to version spec
    private void dependencies(final ConfigurationNode dependencies) throws SerializationException {
        if (!dependencies.isMap()) {
            throw new SerializationException(dependencies, Map.class, "Dependencies must be specified as a map of <alias> => <string or map>");
        }

        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : dependencies.childrenMap().entrySet()) {
            final String alias = String.valueOf(entry.getKey());
            final ConfigurationNode dep = entry.getValue();

            if (dep.isMap()) {
                final @Nullable String group = dep.node("group").getString();
                @Nullable String name = dep.node("name").getString();
                if (name == null && dep.hasChild("artifact")) {
                    name = dep.node("artifact").getString();
                }
                final String moduleInfo = dep.node("module").getString(); // <group>:<name>
                final @Nullable GradleVersion version = dep.node("version").get(GradleVersion.class);
                final VersionCatalogBuilder.LibraryAliasBuilder build;
                if (group == null || name == null) {
                    if (moduleInfo == null) {
                        throw new SerializationException(dep, VersionCatalogBuilder.LibraryAliasBuilder.class, "Either group and name, or module fields must be specified for an alias!");
                    }
                    if (group != null || name != null) {
                        throw new SerializationException(dep, VersionCatalogBuilder.LibraryAliasBuilder.class, "If the 'module' key is used, the 'group' and 'name' fields cannot be specified as they are redundant.");
                    }
                    final String[] elements = moduleInfo.split(":");
                    if (elements.length == 3 && version == null) {
                        this.builder.alias(alias).to(moduleInfo);
                        continue;
                    } else if (elements.length < 2) {
                        throw new SerializationException(dep, VersionCatalogBuilder.LibraryAliasBuilder.class, "A module specification must be in group:artifact[:version] format. To specify element separately, use the 'group' and 'name' keys in the map.");
                    }
                    build = this.builder.alias(alias).to(elements[0], elements[1]);
                } else {
                    build = this.builder.alias(alias).to(group, name);
                }
                if (version == null) {
                    build.withoutVersion();
                    continue;
                }

                final @Nullable String versionRef = version.versionRef();
                if (versionRef != null) {
                    build.versionRef(versionRef);
                } else {
                    build.version(version::applyTo);
                }
            } else {
                final String gav = dep.getString();
                if (gav == null) {
                    throw new SerializationException(dep, String.class, "Unable to get a String or Map value for a dependency");
                }
                this.builder.alias(alias).to(gav);
            }
        }
    }

    private void bundles(final ConfigurationNode bundles) throws SerializationException {
        if (!bundles.isMap()) {
            throw new SerializationException(bundles, MAP_STRING_LIST_STRING, "Bundles must be specified as a map of <bundle name> => list of aliases");
        }

        // TODO: Support anonymous aliases for more convenient specification
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : bundles.childrenMap().entrySet()) {
            this.builder.bundle(String.valueOf(entry.getKey()), entry.getValue().getList(String.class, Collections.emptyList()));
        }
    }

    // A map of String plugin id => String version
    private void plugins(final ConfigurationNode plugins) throws SerializationException {
        if (!plugins.isMap()) {
            throw new SerializationException(plugins, MAP_STRING_STRING, "Plugins must be specified as a map of id => version");
        }
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : plugins.childrenMap().entrySet()) {
            this.plugins.id(String.valueOf(entry.getKey())).version(entry.getValue().get(String.class));
        }
    }

    private void versions(final ConfigurationNode versions) throws SerializationException {
        if (!versions.isMap()) {
            throw new SerializationException(versions, MAP_STRING_GRADLEVERSION, "Version references must be specified as a map of <ref id> => version specification");
        }

        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : versions.childrenMap().entrySet()) {
            final String reference = String.valueOf(entry.getKey()); // TODO: Do we need to do validation on this? or can Gradle
            final GradleVersion version = entry.getValue().get(GradleVersion.class);

            if (version == null) {
                throw new SerializationException(entry.getValue(), GradleVersion.class, "Must have a non-null version value");
            }

            if (version.versionRef() != null) {
                // TODO: Maybe enable this?
                throw new SerializationException(entry.getValue(), GradleVersion.class, "Version reference cannot point to another reference");
            }

            try {
                this.builder.version(reference, version::applyTo);
            } catch (final InvalidUserDataException ex) {
                throw new SerializationException(entry.getValue(), GradleVersion.class, ex.getMessage(), ex.getCause());
            }
        }
    }

}
