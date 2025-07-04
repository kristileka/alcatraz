plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version Dependencies.KOTLIN_VERSION
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("alcatraz")
}

group = "test"
version = "0.0.1-SNAPSHOT"

alcatraz {
    packageName.set("spring.test")

    integrity {
        enabled.set(true)
        token.set("test")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(Dependencies.JVM_TARGET)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
// Ensure code generation happens before compilation
tasks.compileKotlin {
    dependsOn("generateAlcatrazClasses")
}
