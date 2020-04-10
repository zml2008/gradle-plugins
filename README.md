# Gradle Plugins

These are a collection of gradle plugins used for the projects I maintain, designed to reduce boilerplate and apply reasonable defaults.

## Opinionated Defaults

Applies a few settings that are common across Java projects

- Sets project groupid and version from the root project
- Applies the licenser and maven publish plugins
- Limits licensing plugin to only java, kotlin, groovy, and scala files, and excludes files (such as generated sources) in the build directory
- Automatically build source and javadoc jars
- Use UTF-8 for java and javadoc encodings
- Configure all archive tasks for reproducible archives

## Opinionated Kotlin Defaults

Applies a few settings specific to Kotlin JVM projects, and brings in standard opinionated defaults.

- Depend on the Kotlin jdk8 stdlib
- Set the language level to whatever is chosen for Java
- Enable use of JvmDefault on interfaces

## Localization

Using a single template, generate source files to more reliably access strings contained in resources bundles in the `src/<set>/messages` root. Currently supports automatically configuring compile tasks for Java and Kotlin.

## Templating

Given GString templates in `src/<set>/templates`, source will be generated into a generated source root. 

Tasks are named `generate[Set]Templates`, and extend Copy for easy customization. Template properties may be provided using the `GenerateTemplateTask.properties()` method. If no properties are provided, the template context will contain one property, `project` set to the project object.

## Minecraft Dependencies (not actually a plugin)

These are just a series of Kotlin extension functions that allow easily declaring common dependencies in the Minecraft ecosystem
