import org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions
import org.javamodularity.moduleplugin.extensions.ModularityExtension
import org.javamodularity.moduleplugin.extensions.TestModuleOptions

plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
    id("org.javamodularity.moduleplugin")
}

repositories {
    mavenCentral()
}

val errorproneVersion = "2.8.0"
val junitVersion = "5.7.1"

val xsd by configurations.creating {
    isTransitive = false
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
    implementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
    implementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
}

tasks {
    test {
        useJUnitPlatform()
        maxParallelForks = 4
        extensions.configure(TestModuleOptions::class) {
            // test-harness modules do not support Java module
            // https://github.com/spotbugs/spotbugs/issues/1627
            runOnClasspath = true
        }
    }
    compileTestJava {
        extensions.configure(CompileTestModuleOptions::class) {
            isCompileOnClasspath = true
        }
    }
}

configure<ModularityExtension> {
    standardJavaRelease(11)
}

spotless {
    java {
        removeUnusedImports()
        googleJavaFormat()
        licenseHeaderFile("$rootDir/buildSrc/license-header.java")
    }
    groovyGradle {
        target("**/*.gradle")
        greclipse()
        indentWithSpaces()
    }
    kotlinGradle {
        ktlint()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                groupId = "com.github.spotbugs"
                artifactId = "spotbugs-jspecify-plugin"
                name.set("SpotBugs JSpecify Plugin")
                description.set("SpotBugs plugin that works with new standard annotation definitions artifact")
                url.set("https://github.com/spotbugs/spotbugs-jspecify-plugin")
                licenses {
                    license {
                        name.set("GNU Affero General Public License, Version 3.0")
                    }
                    url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                }
                scm {
                    connection.set("scm:git:git@github.com:spotbugs/spotbugs-jspecify-plugin.git")
                    developerConnection.set("scm:git:git@github.com:spotbugs/spotbugs-jspecify-plugin.git")
                    url.set("https://github.com/spotbugs/spotbugs-jspecify-plugin/")
                }
            }
        }
    }
}

val unzipXsd = tasks.register<Copy>("unzipXsd") {
    from(zipTree(xsd.singleFile).matching {
        include("findbugsplugin.xsd")
        include("messagecollection.xsd")
    })
    into("$buildDir/xsd")
}

val validateFindBugsXml = tasks.register("validateFindBugsXml") {
    doFirst {
        ant.withGroovyBuilder {
            "schemavalidate" (
                    "file" to "src/main/resources/findbugs.xml",
                    "noNamespaceFile" to "$buildDir/xsd/findbugsplugin.xsd"
                    ) {
                        "schema"(
                                "namespace" to "http://www.w3.org/2001/XMLSchema-instance",
                                "file" to "$buildDir/xsd/findbugsplugin.xsd"
                                )
                    }
        }
    }
    dependsOn(unzipXsd)
}

val validateMessageXml = tasks.register("validateMessageXml") {
    doFirst {
        ant.withGroovyBuilder {
            "schemavalidate" (
                    "file" to "src/main/resources/messages.xml",
                    "noNamespaceFile" to "$buildDir/xsd/messagecollection.xsd"
                    ) {
                        "schema" (
                                "namespace" to "http://www.w3.org/2001/XMLSchema",
                                "file" to "$buildDir/xsd/messagecollection.xsd"
                                )
                    }
        }
    }
    dependsOn(unzipXsd)
}

val validateXml = tasks.register("validateXml") {
    dependsOn(validateFindBugsXml)
    dependsOn(validateMessageXml)
}

tasks.named("check") {
    dependsOn(validateXml)
}
