pluginManagement {
    repositories {
        maven { setUrl ("https://oss.sonatype.org/content/repositories/snapshots/") }
        gradlePluginPortal()
    }
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.jetbrains.intellij") version "1.10.0"
    }
}

rootProject.name = "Purescript"
include("lexer")