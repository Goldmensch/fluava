plugins {
    id("dev.goldmensch.fluava.convention.java")
    id("dev.goldmensch.fluava.convention.maven-central-deploy")
}

group = "dev.goldmensch"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":cldr-plurals"))
    implementation("io.github.parseworks:parseworks:0.1.1")
    implementation("org.slf4j:slf4j-api:2.0.17")

    api("io.github.kaktushose:proteus:0.2.3")
}