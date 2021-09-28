import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
}

group = "io.github.daniel-tucano"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.github.daniel-tucano:geomez-core:0.1.6-SNAPSHOT")
    implementation("io.github.daniel-tucano:geomez-visualization:0.1.1-SNAPSHOT")
    implementation("org.litote.kmongo:kmongo:4.2.8")
    implementation("io.github.daniel-tucano:matplotlib4k:0.2.8")
    implementation("org.ejml:ejml-simple:0.41")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}