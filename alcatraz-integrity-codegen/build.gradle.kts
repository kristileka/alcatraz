plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":alcatraz-codegen"))
    implementation(Dependencies.cliktCommand())
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}
