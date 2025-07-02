package alcatraz.integrity.api

interface IntegrityTokenValidator {
    fun validateTokenData(data: String)
}