plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":cldr-plurals"))
    implementation("io.github.parseworks:parseworks:0.1.1")
    implementation("org.slf4j:slf4j-api:2.0.17")

    implementation("ch.qos.logback", "logback-core", "1.5.6")
    runtimeOnly("ch.qos.logback", "logback-classic", "1.5.6")

    implementation("io.github.kaktushose:proteus:0.1.2")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit4 test framework
            useJUnit("4.13.2")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}
