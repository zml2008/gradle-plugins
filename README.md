# Gradle Plugins

These are a collection of gradle plugins used for the projects I maintain, designed to reduce boilerplate and apply reasonable defaults.

## Example

An example Kotlin DSL format:

```kotlin
plugins {
    id("ca.stellardrift.opinionated") version "2.0"
    id("ca.stellardrift.opinionated.publish") version "2.0"
}

repositories {
    sponge()
}

opinionated {
    github("zml2008", "gradle-plugins")
    gpl3()
    publication?.apply {
        from(configurations["java"])
    }
}
```

## [Opinionated Defaults](https://plugins.gradle.org/plugin/ca.stellardrift.opinionated)

Applies a few settings that are common across Java projects. Most are configurable through the `opinionated` extension.

- Sets project groupId, version, and description from the root project
- Applies the licenser plugin, and if a `LICENSE_HEADER` file is present in the root project, configures all subprojects to use that file.
- Limits licensing plugin to only java, kotlin, groovy, and scala files, and excludes files (such as generated sources) in the build directory
- Automatically builds source and javadoc jars
- Uses UTF-8 for java and javadoc encodings
- Automatically links to the appropriate JDK javadoc for the targeted Java version
- Configures all archive tasks for reproducible archives
- Activates all compiler warnings, except for requiring `serialVersionUid`, and doclint warnings
- Allows configuring JUnit 5 with the `useJUnit5()` method on the extension. If a custom JUnit version is desired, the version can be configured with the `version.junit` property.
- Applies workarounds for JDK bugs to build properly from JDK8 through current
- If a checkstyle configuration is present at `$rootProject/etc/checkstyle/checkstyle.xml`, configures the checkstyle plugin to use it
- Applies [ktlint](https://github.com/JLLeitschuh/ktlint-gradle) plugin to lint Gradle kotlin build files

## [Opinionated Publication](https://plugins.gradle.org/plugin/ca.stellardrift.opinionated.publish)
This plugin sets up publishing, with a publication named `maven`, applying appropriate options from the project, and configuring publication to `bintray`

Authentication to bintray is done with the following project properties or environment variables

Property | Environment Variable | Purpose
-------- | -------------------- | --------
`bintrayUser` | `BINTRAY_USER` | Username
`bintrayKey` | `BINTRAY_KEY`  | API Key
`bintrayRepo` | `BINTRAY_REPO` | Repository name

The `bintrayUpload` task is restricted to only run when the local checkout is clean, the project's version does not contain `SNAPSHOT`, and the current commit points to the latest tag.

The signing plugin is configured to sign any publication, but will only run on release builds or if the `forceSign` property is set.



## [Opinionated Kotlin Defaults](https://plugins.gradle.org/plugin/ca.stellardrift.opinionated.kotlin)

Applies a few settings specific to Kotlin JVM projects, and brings in standard opinionated defaults.

- Depend on the Kotlin jdk8 stdlib
- Set the language level to whatever is chosen for Java
- Enable use of JvmDefault on interface methods
- Inherits `ktlint` plugin from opinionated plugin to enforce standard formatting

## [Opinionated Fabric Defaults](https://plugins.gradle.org/plugin/ca.stellardrift.opinionated.fabric)
**Note:** Requires the Fabric maven repository to be added to the `settings.gradle.kts`

- Applies the Fabric Loom plugin
- Sets up a `testmod` source set for a test mod that is loaded in the Gradle run tasks
- Configures any Javadoc generation tasks to link to the appropriate versions of Yarn and Fabric API javadocs.

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

Given GString templates in `src/<set>/templates`, source will be generated into a generated source root. 

Tasks are named `generate[SourceSet]Templates`, and extend Copy for easy customization. Template properties may be provided using the `GenerateTemplateTask.properties()` method. If no properties are provided, the template context will contain one property, `project` set to the project object.

## [Configurate Transformations](https://plugins.gradle.org/plugin/ca.stellardrift.configurate-transform)

Use a Configurate loader to transform any resources in a copy task.

The loader can both convert file formats, and load and modify configurations while keeping the same format. Use the `ContentFilterable.convertFormat` and `ContentFilterable.transform` extension functions to add filtering steps to any selection of copied files.

Another mode is validation mode -- using the `ContentFilterable.validate` extension method, any matching files will be validated for any syntax errors. 

The plugin ships with handling for all of Configurate's built-in formats, but any others can be added by implementing the `ConfigProcessor` interface.

## Minecraft Dependencies (not actually a plugin)

These are just a series of Kotlin extension functions that allow easily declaring common dependencies in the Minecraft ecosystem
