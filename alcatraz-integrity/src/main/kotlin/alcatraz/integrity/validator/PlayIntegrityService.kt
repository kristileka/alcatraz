package alcatraz.integrity.validator

import alcatraz.integrity.google.GoogleJWTProvider
import alcatraz.integrity.model.PlayIntegrityEnvelope
import java.net.URI

interface PlayIntegrityService {
    companion object {
        @JvmStatic
        val APP_INTEGRITY_URL: String = "https://playintegrity.googleapis.com/v1/"

        private fun generateURL(packageName: String): URI = URI.create("$APP_INTEGRITY_URL$packageName:decodeIntegrityToken")
    }

    fun validate(token: String): PlayIntegrityEnvelope

    class PlayIntegrityServiceFactory {
        fun create(packageName: String): PlayIntegrityService =
            PlayIntegrityServiceImpl(
                url = generateURL(packageName),
                userMessageValidator =
                    object : IntegrityTokenValidator {
                        override fun validateTokenData(data: String) = Unit
                    },
                googleJWTProvider =
                    object : GoogleJWTProvider {
                        override fun get() = ""
                    },
            )
    }
}
