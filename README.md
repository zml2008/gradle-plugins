# Gradle Plugins

These are a collection of gradle plugins used for the projects I maintain, designed to reduce boilerplate and apply reasonable defaults.

## Opinionated Defaults (`ca.stellardrift.build.opinionated`)

Applies a few settings that are common across Java projects. Most are configurable through the `opinionated` extension.

- Sets project groupId, version from the root project, and description
- Applies the licenser and maven publish plugins
- Limits licensing plugin to only java, kotlin, groovy, and scala files, and excludes files (such as generated sources) in the build directory
- Automatically build source and javadoc jars
- Use UTF-8 for java and javadoc encodings
- Configure all archive tasks for reproducible archives
- Activate all compiler warnings, except for requiring `serialVersionUid`, and doclint warnings
- Allows configuring JUnit 5 with the `useJUnit5()` method on the extension. If a custom JUnit version is desired, the version can be configured with the `version.junit` property.

### Publication
This plugin sets up publishing, with a publication named `maven`, applying appropriate options from the project, and configuring publication to `bintray`

Authentication to bintray is done with the following project properties or environment variables

Property | Environment Variable | Purpose
-------- | -------------------- | --------
`bintrayUser` | `BINTRAY_USER` | Username
`bintrayKey` | `BINTRAY_KEY`  | API Key
`bintrayRepo` | `BINTRAY_REPO` | Repository name

The `bintrayUpload` task is restricted to only run when the local checkout is clean, the project's version does not contain `SNAPSHOT`, and the current commit points to the latest tag.

The signing plugin is configured to sign any publication, but will only run on release builds or if the `forceSign` property is set.


### Example

An example Kotlin DSL format:

```kotlin
plugins {
    id("ca.stellardrift.build.opinionated") version "1.0"
}

repositories {
    sponge()
}

opinionated {
    github("zml2008", "gradle-plugins")
    license.set(gpl3())
    publication.apply {
        from(configurations["java"])
    }
}
```

## Opinionated Kotlin Defaults (`ca.stellardrift.build.opinionated.kotlin`)

Applies a few settings specific to Kotlin JVM projects, and brings in standard opinionated defaults.

- Depend on the Kotlin jdk8 stdlib
- Set the language level to whatever is chosen for Java
- Enable use of JvmDefault on interface methods

## Localization (`ca.stellardrift.build.localization`)

Using a single template, generate source files to more reliably access strings contained in resources bundles in the `src/<set>/messages` root. Currently supports automatically configuring compile tasks for Java and Kotlin.

## Templating (`ca.stellardrift.build.templating`)

Given GString templates in `src/<set>/templates`, source will be generated into a generated source root. 

Tasks are named `generate[SourceSet]Templates`, and extend Copy for easy customization. Template properties may be provided using the `GenerateTemplateTask.properties()` method. If no properties are provided, the template context will contain one property, `project` set to the project object.

## Minecraft Dependencies (not actually a plugin)

These are just a series of Kotlin extension functions that allow easily declaring common dependencies in the Minecraft ecosystem
