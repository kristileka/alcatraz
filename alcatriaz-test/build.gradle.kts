plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(Dependencies.guice())
    testImplementation(kotlin("test"))
    implementation(project(":alcatriaz-devicecheck"))
    implementation(project(":alcatriaz-server"))
    implementation(project(":alcatriaz-core"))
}

tasks.test {
    useJUnitPlatform()
}
