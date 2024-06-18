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
    implementation("io.github.daniel-tucano:geomez-visualization:0.1.0-SNAPSHOT")
    implementation("org.litote.kmongo:kmongo:4.4.0")
    implementation("io.github.daniel-tucano:matplotlib4k:0.2.7")
    implementation("org.ejml:ejml-simple:0.41")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-core:1.5.6")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.hibernate:hibernate-core:5.4.21.Final")
    implementation("org.hibernate:hibernate-entitymanager:5.4.21.Final")
    implementation("mysql:mysql-connector-java:8.0.21")
    implementation(kotlin("stdlib-jdk8"))
    implementation(files("C:\\Program Files\\Polyspace\\R2021a\\extern\\engines\\java\\jar\\engine.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}