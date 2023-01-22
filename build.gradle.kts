import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion: String by project
val version: String by project
val ideaVersion: String by project
val intellijPublishToken: String by project
val publishChannels: String by project

plugins {
    java
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.12.0"
    id("org.jetbrains.grammarkit") version "2021.2.2"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(javaVersion.toInt())
}

// Plugin config
intellij {
    pluginName.set("purity-intellij")
    version.set(ideaVersion)
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
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
    generateLexer.configure {
        source.set("src/main/grammar/Purescript.flex")
        targetDir.set("src/main/gen/org/purescript/lexer/")
        targetClass.set("_PSLexer")
        purgeOldFiles.set(true)
        skeleton.set(file("src/main/grammar/idea-flex.skeleton"))
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        dependsOn(generateLexer)
    }
    withType<KotlinCompile>().configureEach { dependsOn(generateLexer) }
}
sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/gen"))
        }
    }
}
