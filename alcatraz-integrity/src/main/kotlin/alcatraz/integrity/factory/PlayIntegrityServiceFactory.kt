package alcatraz.integrity.factory

import alcatraz.integrity.Constants.APP_INTEGRITY_URL
import alcatraz.integrity.google.GoogleJWTProvider
import alcatraz.integrity.validator.IntegrityTokenValidator
import alcatraz.integrity.validator.PlayIntegrityService
import alcatraz.integrity.validator.PlayIntegrityServiceImpl
import com.google.inject.Provider
import java.net.URI

object PlayIntegrityServiceFactory {
    private fun generateURL(packageName: String): URI = URI.create("$APP_INTEGRITY_URL$packageName:decodeIntegrityToken")

    fun create(
        packageName: String,
        jwtProvider: Provider<String>,
    ): PlayIntegrityService =
        PlayIntegrityServiceImpl(
            url = generateURL(packageName),
            userMessageValidator =
                object : IntegrityTokenValidator {
                    override fun validateTokenData(data: String) = Unit
                },
            googleJWTProvider =
                object : GoogleJWTProvider {
                    override fun get() = jwtProvider.get()
                },
        )
}
