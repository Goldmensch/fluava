import cldrgenerator.CLDRGenerateTask

plugins {
    id("dev.goldmensch.fluava.convention.java")
    id("dev.goldmensch.fluava.convention.maven-central-deploy")
}

group = "dev.goldmensch.fluava"

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

tasks.sourcesJar {
    dependsOn("generatePlurals")
}

tasks.processResources {
    dependsOn("generatePlurals")
}

tasks.test {
    useJUnitPlatform()
}