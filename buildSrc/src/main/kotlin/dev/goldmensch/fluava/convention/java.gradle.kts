package dev.goldmensch.fluava.convention

import org.gradle.kotlin.dsl.`java-library`

plugins {
    `java-library`
}

java {
    targetCompatibility = JavaVersion.VERSION_25
    sourceCompatibility = JavaVersion.VERSION_25

    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }

    withSourcesJar()
    withJavadocJar()
}