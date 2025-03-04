plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(Dependencies.guice())
}

tasks.test {
    useJUnitPlatform()
}
