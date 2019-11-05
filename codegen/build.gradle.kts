import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    kotlin("kapt") version "1.3.50"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":annotations"))
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    implementation("com.squareup:kotlinpoet:1.3.0")

    testCompile("junit", "junit", "4.12")

    kapt("com.google.auto.service:auto-service:1.0-rc4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
