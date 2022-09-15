plugins {
    id("com.github.spotbugs.jspecify")
    id("org.javamodularity.moduleplugin")
}

repositories {
    mavenCentral()
}

val spotbugsVersion = "4.7.1"
val jspecifyVersion = "0.2.0"

dependencies {
    compileOnly("com.github.spotbugs:spotbugs:$spotbugsVersion")
    compileOnly("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    testImplementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.18.0")
    testImplementation("com.github.spotbugs:spotbugs:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness-core:$spotbugsVersion")
    testImplementation("com.github.spotbugs:test-harness-jupiter:$spotbugsVersion")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.jspecify:jspecify:$jspecifyVersion")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("com.google.guava:guava:31.1-jre")
    xsd("com.github.spotbugs:spotbugs:$spotbugsVersion")
}

defaultTasks("spotlessApply", "build")
