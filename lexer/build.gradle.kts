import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("org.jetbrains.intellij")
    id("org.jetbrains.grammarkit") version "2021.1.3"
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://cache-redirector.jetbrains.com/intellij-dependencies") }
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

    register<GenerateLexer>("generateLexer") {
        source = "src/main/grammar/Purescript.flex"
        targetDir = "src/main/gen/org/purescript/lexer/"
        targetClass = "_PSLexer"
        purgeOldFiles = true
        skeleton = "src/main/grammar/idea-flex.skeleton"
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "11"
        targetCompatibility = "11"

        dependsOn("generateLexer")
    }
    withType<KotlinCompile>()
        .configureEach {
            kotlinOptions { jvmTarget = "11" }
            dependsOn("generateLexer")
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