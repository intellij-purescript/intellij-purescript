import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij")
    id("org.jetbrains.grammarkit") version "2021.2.2"
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

version = "1.15-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
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
        options.release.set(11)
        dependsOn(generateLexer)
    }
    withType<KotlinCompile>()
        .configureEach {
            kotlinOptions { jvmTarget = "11" }
            dependsOn(generateLexer)
        }
}
sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/gen", "src/main/java"))
        }
    }
}
val ideaVersion: String by project
intellij {
    version.set(ideaVersion)
}