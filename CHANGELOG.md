# 3.0

- Divide plugins into submodules to reduce which dependencies need to be pulled in. **This will change artifact IDs and classes, so this is technically a breaking change.
- Remove `implementationInclude`/`apAnd`-type methods since it turns out Gradle has a way to do that already.
- Apply ktlint plugin for buildscript linting
- If a file named `LICENSE_HEADER` exists in the project root, it will now be used for licensing
- Automatically add a Javadoc link to the appropriate JDK version
- Only apply doclint compiler option on JDK 9+ to work around a JDK 8 bug
- Add `ca.stellardrift.opinionated.fabric` plugin for building Fabric mods
- The `checkstyle` plugin will now be applied when a `etc/checkstyle/checkstyle.xml` file exists
- Add a convenient way to declare publishing repositories that will only be added if the appropriate credentials are provided as gradle properties

# 2.0.1

- Apply Javadoc options to maximize compatibility
- mc: Add velocity snapshot and release repositories
- templating: Add templates to both Java and Kotlin compile source paths when both plugins are present
- Produce more accurate SCM urls

# 2.0

- Split publishing options into their own plugin -- some projects have more custom needs on one or both of publishing and compilation
- Fix releasing when multiple tags are defined
- Allow releasing any tag, not just the latest
- Correct date format for Bintray

# 1.0.1

- Localization plugin now works on non-kotlin projects
- JUnit 5 dependencies are applied to the appropriate scope
- Localization and templating plugins now generate sources following matching patch schemes
- License short names now match those on bintray
- License setters are now methods on the extension class, not standalone extension functions.
- Use the `--release` javac argument to enforce the usage of correct library methods on JDKs >= 9
- Opinionated plugin will function on projects not managed with git

# 1.0

- Initial release
