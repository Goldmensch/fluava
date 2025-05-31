package dev.goldmensch.fluava.convention

import org.jreleaser.model.Active
import org.jreleaser.model.Signing
import software.amazon.awssdk.core.internal.signer.SigningMethod

plugins {
    `maven-publish`
    id("org.jreleaser")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Fluava")
                description.set("A modern, fast java implementation of the amazing project fluent")
                url.set("https://github.com/Goldmensch/fluava")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://mit-license.org/")
                    }
                }

                developers {
                    developer {
                        name.set("Nick Hensel")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/goldmensch/fluava.git")
                    developerConnection.set("scm:git:ssh://github.com/goldmensch/fluava.git")
                    url.set("https://github.com/Goldmensch/fluava")
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        copyright = "Nick Hensel"
    }


    signing {
        active = Active.ALWAYS
        armored = true
//        mode = Signing.Mode.COMMAND
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    setStage("UPLOAD")
                }
            }
        }
    }
}

tasks.jreleaserDeploy {
    dependsOn(tasks.publish)
}