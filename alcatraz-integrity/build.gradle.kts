plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":alcatraz-common"))
    implementation(Dependencies.coroutinesCore())
    implementation(Dependencies.guice())
    implementation(Dependencies.jacksonKotlin())
    implementation(Dependencies.jacksonDateFormat())
    implementation(Dependencies.caffeine())
}
