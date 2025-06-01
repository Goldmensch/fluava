plugins {
    id("io.freefair.aggregate-javadoc") version("8.12.2.1")
}

repositories {
    mavenCentral()
}

dependencies {
    javadoc(project(":fluava"))
    javadoc(project(":cldr-plurals"))
}

allprojects {
    version = "0.1.1"
}

subprojects {
    tasks.withType<Javadoc> {
        val options = options as StandardJavadocDocletOptions

        options.encoding = "UTF-8"
        options.addBooleanOption("Xdoclint:none,-missing", true)
        options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
    }
}

//tasks.withType<Javadoc>().configureEach {
//    val options = options as StandardJavadocDocletOptions
//    options.overview = "src/main/javadoc/overview.md"
//    options.links = listOf()
//
//    doLast {
//        copy {
//            include("**/doc-files/*")
//            from("src/main/javadoc")
//            into(project.layout.buildDirectory.dir("docs/javadoc"))
//        }
//    }
//}