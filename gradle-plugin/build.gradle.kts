plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.elharo.docfix"
version = "1.0.6-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.elharo.docfix:docfix:1.0.6-SNAPSHOT")
    
    testImplementation("junit:junit:4.13.2")
    
    // For functional tests
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("docfixPlugin") {
            id = "com.elharo.docfix"
            implementationClass = "com.elharo.docfix.gradle.DocFixPlugin"
            displayName = "DocFix Gradle Plugin"
            description = "Gradle plugin that fixes Javadoc comments to conform to Oracle Javadoc guidelines"
        }
    }
}

// Configure functional tests
sourceSets {
    create("functionalTest") {
        java {
            srcDir("src/functionalTest/java")
        }
        resources {
            srcDir("src/functionalTest/resources")
        }
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

configurations {
    getByName("functionalTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    getByName("functionalTestRuntimeOnly") {
        extendsFrom(configurations["testRuntimeOnly"])
    }
}

val functionalTest by tasks.registering(Test::class) {
    description = "Runs functional tests"
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(functionalTest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "docfix-gradle-plugin"
            
            pom {
                name.set("DocFix Gradle Plugin")
                description.set("Gradle plugin that fixes Javadoc comments to fit Oracle guidelines")
                url.set("https://github.com/elharo/docfix")
                
                licenses {
                    license {
                        name.set("GPL v3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                
                developers {
                    developer {
                        id.set("elharo")
                        name.set("Elliotte Rusty Harold")
                        email.set("elharo@ibiblio.org")
                        url.set("https://www.elharo.com")
                        timezone.set("America/New_York")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/elharo/docfix.git")
                    developerConnection.set("scm:git:ssh://github.com:elharo/docfix.git")
                    url.set("https://github.com/elharo/docfix/tree/master")
                }
            }
        }
    }
}
