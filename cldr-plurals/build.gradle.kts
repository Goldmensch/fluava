import cldrgenerator.CLDRGenerateTask

plugins {
    id("java")
}

group = "io.github.goldmensch"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.register<CLDRGenerateTask>("generatePlurals")

tasks.compileJava {
    dependsOn("generatePlurals")
}

tasks.processResources {
    dependsOn("generatePlurals")
}

tasks.test {
    useJUnitPlatform()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}