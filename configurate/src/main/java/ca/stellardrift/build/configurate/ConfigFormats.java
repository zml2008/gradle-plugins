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

import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Standard formats for configuration
 */
public final class ConfigFormats {
    public static final ConfigProcessor<HoconConfigurationLoader.Builder, HoconConfigurationLoader> HOCON = new ConfigProcessor<>(HoconConfigurationLoader::builder);
    public static final ConfigProcessor<GsonConfigurationLoader.Builder, GsonConfigurationLoader> JSON = new ConfigProcessor<>(GsonConfigurationLoader::builder);
    public static final ConfigProcessor<YamlConfigurationLoader.Builder, YamlConfigurationLoader> YAML = new ConfigProcessor<>(YamlConfigurationLoader::builder);
    public static final ConfigProcessor<XmlConfigurationLoader.Builder, XmlConfigurationLoader> XML = new ConfigProcessor<>(XmlConfigurationLoader::builder);

    private ConfigFormats() {
    }

}
