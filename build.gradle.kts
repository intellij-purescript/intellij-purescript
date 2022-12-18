import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val javaVersion: String by project
val version: String by project
val ideaVersion: String by project
val intellijPublishToken: String by project
val publishChannels: String by project

buildscript {
    repositories {
        mavenCentral()
        maven { setUrl("https://cache-redirector.jetbrains.com/intellij-dependencies") }
    }
}

plugins {
    id("org.jetbrains.intellij")
    java
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

dependencies {
    implementation(project(":lexer"))
    implementation(kotlin("stdlib"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

// Plugin config
intellij {
    pluginName.set("purity-intellij")
    version.set(ideaVersion)

}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion
        }
    }
    getByName<Test>("test") {
        useJUnitPlatform()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    
    publishPlugin {
        token.set(intellijPublishToken)
        channels.set(listOf(publishChannels))
    }

    patchPluginXml {
        sinceBuild.set("223")
    }
}
