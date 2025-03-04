import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version Dependencies.KOTLIN_VERSION
    kotlin("plugin.serialization") version Dependencies.KOTLIN_VERSION
    id("idea")
}

repositories {
    google()
    mavenCentral()
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(Dependencies.JVM_TARGET))
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        allWarningsAsErrors.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}
