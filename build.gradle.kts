plugins {
    id("com.github.spotbugs.jspecify")
    id("org.javamodularity.moduleplugin")
}

repositories {
    mavenCentral()
}

val spotbugsVersion = "4.5.3"
val jspecifyVersion = "0.2.0"

dependencies {
    compileOnly("com.github.spotbugs:spotbugs:$spotbugsVersion")
    compileOnly("org.jspecify:jspecify:$jspecifyVersion")
    compileOnly("org.slf4j:slf4j-api:2.0.0-alpha5")
    testImplementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.1")
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
