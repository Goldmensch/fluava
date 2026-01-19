plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.json:json:20240303")
    implementation("io.github.parseworks:parseworks:0.1.1")
    implementation("com.palantir.javapoet:javapoet:0.6.0")

    implementation("org.jreleaser:org.jreleaser.gradle.plugin:1.18.0")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}