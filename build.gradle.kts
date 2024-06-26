buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.sonarqube") version "4.4.1.3373"
    kotlin("jvm") version "1.8.0" // Adjust to your Kotlin version
    kotlin("plugin.serialization") version "1.8.0" // Same version as Kotlin
}

sonar {
    properties {
        property("sonar.projectKey", "Study-Buddies-SwEnt_SwEnt_Group_project")
        property("sonar.organization", "study-buddies-swent")
        property("sonar.host.url", "https://sonarcloud.io")
        property ("sonar.gradle.skipCompile", "true")
    }
}