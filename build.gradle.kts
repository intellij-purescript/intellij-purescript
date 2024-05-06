val javaVersion: String by project
val version: String by project
val ideaVersion: String by project
val intellijPublishToken: String by project
val publishChannels: String by project

plugins {
    java
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
    id("org.jetbrains.grammarkit") version "2022.3.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
    @Suppress("UnstableApiUsage")
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.JETBRAINS
    }
}

// Plugin config
intellij {
    pluginName.set("purity-intellij")
    version.set(ideaVersion)
    plugins.set(listOf("org.intellij.plugins.markdown"))
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }

    
    publishPlugin {
        token.set(intellijPublishToken)
        channels.set(listOf(publishChannels))
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("")
    }
    generateLexer.configure {
        sourceFile.set(file("src/main/grammar/Purescript.flex"))
        targetDir.set("src/main/gen/org/purescript/lexer/")
        targetClass.set("_PSLexer")
        purgeOldFiles.set(true)
        skeleton.set(file("src/main/grammar/idea-flex.skeleton"))
    }
    compileJava.configure { 
        options.encoding = "UTF-8"
    }
}
sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/gen"))
        }
    }
}
