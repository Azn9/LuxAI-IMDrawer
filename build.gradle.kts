plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "dev.azn9"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // gson
    implementation("com.google.code.gson:gson:2.8.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("MainKt")
}