package alcatraz.signature

interface AlcatrazSignatureValidator {
    fun <T, K> validate(
        received: T,
        actual: T,
        using: K,
    ): Boolean
}
