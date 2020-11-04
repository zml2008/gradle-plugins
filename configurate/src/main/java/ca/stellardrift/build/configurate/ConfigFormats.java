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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Standard formats for configuration.
 */
public final class ConfigFormats {

    private static final Map<String, ConfigProcessor<?, ?>> BY_EXTENSION = new HashMap<>();
    private static final Set<ConfigProcessor<?, ?>> PROCESSORS = new HashSet<>();

    public static final ConfigProcessor<HoconConfigurationLoader.Builder, HoconConfigurationLoader> HOCON = register(HoconConfigurationLoader::builder, "conf", "hocon");
    public static final ConfigProcessor<GsonConfigurationLoader.Builder, GsonConfigurationLoader> JSON = register(GsonConfigurationLoader::builder, "json");
    public static final ConfigProcessor<YamlConfigurationLoader.Builder, YamlConfigurationLoader> YAML = register(YamlConfigurationLoader::builder, "yml", "yaml");
    public static final ConfigProcessor<XmlConfigurationLoader.Builder, XmlConfigurationLoader> XML = register(XmlConfigurationLoader::builder, "xml");

    private ConfigFormats() {
    }

    private static <B extends AbstractConfigurationLoader.Builder<B, L>,
            L extends AbstractConfigurationLoader<?>> ConfigProcessor<B, L> register(final Supplier<B> builder,
                                                                                     String... extensions) {
        final ConfigProcessor<B, L> ret = new ConfigProcessor<>(builder, extensions);
        PROCESSORS.add(ret);
        for (final String ext : extensions) {
            BY_EXTENSION.put(ext, ret);
        }
        return ret;
    }

    public static Set<ConfigProcessor<?, ?>> all() {
        return Collections.unmodifiableSet(PROCESSORS);
    }

    public static @Nullable ConfigProcessor<?, ?> byExtension(final String extension) {
        return BY_EXTENSION.get(extension);
    }
}
