plugins {
    application
    kotlin("jvm")
}

group = "net.reflact"
version = "2026.01.08-1.21.11"

repositories {
    mavenCentral()
    mavenLocal() // To pick up reflact-engine
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":engine"))
    implementation("com.github.Minestom:Minestom:2026.01.08-1.21.11")
    implementation("org.slf4j:slf4j-simple:2.0.16") // implementation for logging
}

application {
    mainClass.set("net.reflact.server.ReflactServer")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
