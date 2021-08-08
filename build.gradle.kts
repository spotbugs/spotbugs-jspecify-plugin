plugins {
    id("com.github.spotbugs.jspecify")
    id("org.javamodularity.moduleplugin")
    id("com.gtramontina.ghooks.gradle") version "1.1.1"
}

repositories {
    mavenCentral()
}

val spotbugsVersion = "4.3.0"
val jspecifyVersion = "0.2.0"

dependencies {
    compileOnly("com.github.spotbugs:spotbugs:$spotbugsVersion")
    compileOnly("org.jspecify:jspecify:$jspecifyVersion")
    testImplementation("com.github.spotbugs:spotbugs:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness-core:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness-jupiter:$spotbugsVersion")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.jspecify:jspecify:$jspecifyVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    xsd("com.github.spotbugs:spotbugs:$spotbugsVersion")
}

defaultTasks("spotlessApply", "build")
