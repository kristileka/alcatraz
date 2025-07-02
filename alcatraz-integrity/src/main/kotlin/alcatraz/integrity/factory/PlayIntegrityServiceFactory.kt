package alcatraz.integrity.factory

import alcatraz.integrity.Constants.APP_INTEGRITY_URL
import alcatraz.integrity.api.IntegrityTokenValidator
import alcatraz.integrity.api.PlayIntegrityService
import alcatraz.integrity.runtime.PlayIntegrityServiceImpl
import java.net.URI

object PlayIntegrityServiceFactory {
    private fun generateURL(packageName: String): URI =
        URI.create("$APP_INTEGRITY_URL$packageName:decodeIntegrityToken")

    fun create(
        packageName: String,
        jwtProvider: () -> String,
    ): PlayIntegrityService =
        PlayIntegrityServiceImpl(
            url = generateURL(packageName),
            userMessageValidator =
                object : IntegrityTokenValidator {
                    override fun validateTokenData(data: String) = Unit
                },
            googleJWTProvider = jwtProvider
        )
}
