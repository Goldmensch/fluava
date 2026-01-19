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
    version = "1.2.2"
}


subprojects {
    tasks.withType<Javadoc> {
        val options = options as StandardJavadocDocletOptions

        options.encoding = "UTF-8"
        options.addBooleanOption("Xdoclint:none,-missing", true)
        options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
    }
}