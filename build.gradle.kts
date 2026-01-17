plugins {
    application
}

group = "net.reflact"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal() // To pick up reflact-engine
    maven("https://jitpack.io")
}

dependencies {
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
