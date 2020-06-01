plugins {
  kotlin("jvm") version embeddedKotlinVersion
}

repositories {
  jcenter()
  gradlePluginPortal()
}

dependencies {
  implementation(gradleApi())
  implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")
}
