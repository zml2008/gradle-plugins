# Gradle Plugins

These are a collection of gradle plugins used for the projects I maintain, designed to reduce boilerplate and apply a few more options on top of what [indra] already applies.

## Example

An example Kotlin DSL format:

## [Opinionated Defaults](https://plugins.gradle.org/plugin/ca.stellardrift.opinionated)

This builds on top of the [indra] plugin, doing the following:

- Configures Javadoc to link source
- Adds an option to automatically set up automatic module names for artifacts
- Disables unclaimed annotations warnings from annotation processing
- If a checkstyle configuration is present at `$rootProject/.checkstyle/checkstyle.xml`, configures the checkstyle plugin to use it
- Applies [ktlint](https://github.com/JLLeitschuh/ktlint-gradle) plugin to lint Gradle kotlin build files

## [Localization](https://plugins.gradle.org/plugin/ca.stellardrift.localization)

Using a single template, generate source files to more reliably access strings contained in resources bundles in the `src/<set>/messages` root. Currently supports automatically configuring compile tasks for Java and Kotlin. A messages class template in GString format must be provided. This template will get the following parameters:

Variable | Purpose
-------- | -------
`bundleName` | name of resource bundle for file
`packageName` | file package
`className` | Name of file capitalized for use as a class name
`keys` | The resource bundle keys

This plugin adds a `localization` extension to the project with the following properties:

Property | Type | Use
-------- | ----- | ------
`templateFile` | File property | The location of the template file to use
`templateType` | TemplateType | The language the template is written in

## [Templating](https://plugins.gradle.org/plugin/ca.stellardrift.templating) 

This plugin is no longer published as of version 6.1.0. It has been replaced by 
the [blossom](https://github.com/KyoriPowered/blossom) plugin instead, which provides many more powerful and flexible features.

<details>
<summary>Legacy plugin information</summary>
Given GString templates in `src/<set>/templates`, source will be generated into a generated source root. 

Tasks are named `generate[SourceSet]Templates`, and extend Copy for easy customization. Template properties may be provided using the `GenerateTemplateTask.properties()` method. If no properties are provided, the template context will contain one property, `project` set to the project object.
</details>

## [Configurate Transformations](https://plugins.gradle.org/plugin/ca.stellardrift.configurate-transformations)

Use a Configurate 4 loader to transform any resources in a copy task.

The loader can both convert file formats, and load and modify configurations while keeping the same format. Use the `ContentFilterable.convertFormat` and `ContentFilterable.transform` extension functions to add filtering steps to any selection of copied files.

Another mode is validation mode -- using the `ContentFilterable.validate` extension method, any matching files will be validated for any syntax errors. 

The plugin ships with handling for all of Configurate's built-in formats, but any others can be added by creating new instances of the `ConfigProcessor` class.

## [Stellardrift Repository](https://plugins.gradle.org/plugin/ca.stellardrift.repository)

*(since v6.0.0)* Adds extensions to the Settings and Project `RepositoryHandler`s to easily declare Stellardrift repositories on projects.

These are:

```gradle
repositories {
  stellardrift.releases() // published and proxied releases
  stellardrift.ownReleases() // published releases
  stellardrift.snapshots() // published and proxied snapshots
}
```

## [Polyglot Version Catalog](https://plugins.gradle.org/plugin/ca.stellardrift.polyglot-version-catalogs)

Add support for alternate languages to Gradle's new version catalog feature in 7.0.

This plugin supports all Configurate languages: YAML, JSON, HOCON, and XML.

Apply it in the `settings.gradle[.kts]`, and create a `gradle/libs.versions.{yaml,yml,conf,xml,json}` in the project.

<details>
<summary>Example</summary>

**settings.gradle.kts**
```kotlin
plugins {
    id("ca.stellardrift.polyglot-version-catalogs") version "6.0.0"
}

// [...]
```

**gradle/libs.dependencies.yml**
```yaml
# Basic format metadata
metadata:
  format: {version: 1.0}
  
# Declare versions for project plugins
# This *does not* work for `settings` plugins.
# Declarations here do not actually apply any plugins -- they simply provide default versions.
plugins:
  indra-licenserSpotless: {id: net.kyori.indra.licenser.spotless, version: 3.0.0}
  versions: "com.github.ben-manes.versions:0.38.0"
  shadow: "com.github.johnrengelman.shadow:6.1.0"

# Gradle-style version references
versions:
  junit: 5.7.1

# Declare dependencies to generate accessors for
dependencies:
  # Dependencies using version references
  junitApi: {group: &junit org.junit.jupiter, name: junit-jupiter-api, version: { ref: junit }}
  junitEngine: {group: *junit, name: junit-jupiter-engine, version: { ref: junit }}

  # Plain scalar
  assertj: org.assertj:assertj-core:3.19.0

  # Rich versions
  asm: {module: "org.ow2.asm:asm", version: {strictly: 9.1}}

  # Using yaml anchors to declare versions 
  configurateCore: {group: org.spongepowered, name: configurate-core, version: &configurate 4.0.0}
  configurateHocon: {group: org.spongepowered, name: configurate-hocon, version: *configurate}
  configurateYaml: {group: org.spongepowered, name: configurate-yaml, version: *configurate}

# Dependency bundles, with dependencies from those declared above 
bundles:
  allJunit: [junitApi, junitEngine]
```
</details>

## Minecraft Dependencies (not actually a plugin)

These are just a series of Kotlin extension functions that allow easily declaring common dependencies in the Minecraft ecosystem

[indra]: https://github.com/KyoriPowered/indra
