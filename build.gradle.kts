plugins {
    application
}

group = "net.reflect"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal() // To pick up reflect-engine
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.reflect:reflect-engine:1.0.0-SNAPSHOT")
    implementation("com.github.Minestom:Minestom:2026.01.08-1.21.11")
    implementation("org.slf4j:slf4j-simple:2.0.16") // implementation for logging
}

application {
    mainClass.set("net.reflect.server.ReflectServer")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
