object Dependencies {
    const val JVM_TARGET = "21"
    const val KOTLIN_VERSION = "2.0.20"
    private const val JACKSON_VERSION = "2.18.2"
    private const val COROUTINES_VERSION = "1.10.1"
    private const val GUICE = "7.0.0"
    private const val BOUNCY_CASTLE_VERSION = "1.80"

    fun guice() = "com.google.inject:guice:$GUICE"

    fun coroutinesCore() = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION"

    fun jacksonKotlin() = "com.fasterxml.jackson.module:jackson-module-kotlin:$JACKSON_VERSION"

    fun jacksonDateFormat() = "com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:$JACKSON_VERSION"

    fun bouncyCastle() = "org.bouncycastle:bcpkix-jdk18on:$BOUNCY_CASTLE_VERSION"
}
