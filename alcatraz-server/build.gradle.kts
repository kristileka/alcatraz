plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(Dependencies.guice())
    implementation(project(":alcatraz-core"))
    implementation(project(":alcatraz-devicecheck"))
}

tasks.test {
    useJUnitPlatform()
}
