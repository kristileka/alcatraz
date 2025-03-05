plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}
