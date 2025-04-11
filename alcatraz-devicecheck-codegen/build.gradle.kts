plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.5.2")
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
}
