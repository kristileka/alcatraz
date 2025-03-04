package alcatraz.integrity.model

data class PlayIntegrityEnvelope(
    var packageName: String? = null,
    var requestPackageName: String? = null,
    var deviceIntegrity: String? = null,
    var appIntegrity: String? = null,
    var licenseIntegrity: String? = null,
    var userMessage: String? = null,
)
