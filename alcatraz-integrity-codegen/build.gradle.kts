plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(Dependencies.cliktCommand())
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}
