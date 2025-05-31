package dev.goldmensch.fluava.convention

import org.gradle.kotlin.dsl.`java-library`

plugins {
    `java-library`
}

java {
    targetCompatibility = JavaVersion.VERSION_24
    sourceCompatibility = JavaVersion.VERSION_24

    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }

    withSourcesJar()
    withJavadocJar()
}