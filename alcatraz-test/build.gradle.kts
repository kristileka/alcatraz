plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(Dependencies.guice())
    testImplementation(kotlin("test"))
    implementation(project(":alcatraz-devicecheck"))
    implementation(project(":alcatraz-integrity"))
    implementation(project(":alcatraz-server"))
    implementation(project(":alcatraz-core"))
}

tasks.test {
    useJUnitPlatform()
}
