package alcatraz.server.devicecheck

import alcatraz.devicecheck.receipt.AppleJwsGenerator

data class AlcatrazDeviceCheckProperties(
    val teamIdentifier: String,
    val bundleIdentifier: String,
    val appleJwsGenerator: AppleJwsGenerator,
) {
    companion object {
        const val APPLE_TEAM_IDENTIFIER_LENGTH = 10
    }

    init {
        require(teamIdentifier.length == APPLE_TEAM_IDENTIFIER_LENGTH) {
            "The Apple team identifier must consist of exactly 10 digits"
        }

        require(bundleIdentifier.isNotEmpty()) {
            "The Apple bundle identifier must not be empty"
        }
    }
}
