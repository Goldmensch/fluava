repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20240303")
    implementation("io.github.parseworks:parseworks:0.1.1")
    implementation("com.palantir.javapoet:javapoet:0.6.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}