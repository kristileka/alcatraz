package alcatraz.integrity.runtime

import alcatraz.common.logger
import alcatraz.integrity.api.IntegrityTokenValidator
import alcatraz.integrity.api.PlayIntegrityService
import alcatraz.integrity.exceptions.IntegrityException
import alcatraz.integrity.google.IntegrityHttpClient
import alcatraz.integrity.model.PlayIntegrityEnvelope
import alcatraz.integrity.model.TokenPayloadExternal
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.Provider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.util.*

class PlayIntegrityServiceImpl(
    private val url: URI,
    private val userMessageValidator: IntegrityTokenValidator,
    private val googleJWTProvider: Provider<String>,
) : PlayIntegrityService {

    companion object {
        val objectMapper = jacksonObjectMapper()
        val ALLOWED_DEVICE_RECOGNITION_VERDICT =
            setOf(
                "MEETS_BASIC_INTEGRITY",
                "MEETS_STRONG_INTEGRITY",
                "MEETS_DEVICE_INTEGRITY",
            )
        val NOT_ALLOWED_LIST_RECENT_ACTIVITY = listOf("LEVEL_4", "UNEVALUATED")
        val ALLOWED_ENVIRONMENT_DETAILS = listOf("NO_ISSUES")
    }

    override fun validate(token: String): PlayIntegrityEnvelope =
        runBlocking {
            validateAsync(token)
        }

    private suspend fun validateAsync(
        token: String,
        appleReceiptExchangeHttpClientAdapter: IntegrityHttpClient =
            IntegrityHttpClient.SimpleIntegrityHttpClient,
    ): PlayIntegrityEnvelope =
        coroutineScope {
            val tokenPayloadExternal =
                retrieveIntegrityResponse(
                    token,
                    googleJWTProvider.get(),
                    appleReceiptExchangeHttpClientAdapter,
                )

            launch { validateResponse(tokenPayloadExternal) }
            launch { validateDeviceIntegrity(tokenPayloadExternal.deviceIntegrity) }
            launch { validateAppIntegrity(tokenPayloadExternal.appIntegrity) }
            launch { validateLicenseIntegrity(tokenPayloadExternal.accountDetails) }
            launch { validatePackageName(tokenPayloadExternal) }
            tokenPayloadExternal.deviceIntegrity.recentDeviceActivity?.let {
                launch {
                    validateRecentActivity(
                        it,
                    )
                }
            }

            tokenPayloadExternal.environmentDetails?.let {
                launch {
                    validateEnvironmentDetails(
                        it,
                    )
                }
            }

            val tokenData = decryptIntegrityResponse(tokenPayloadExternal)
            userMessageValidator.validateTokenData(tokenData)
            mapPlayIntegrityEnvelope(
                tokenPayloadExternal,
                tokenData,
            )
        }

    private fun validatePackageName(tokenPayloadExternal: TokenPayloadExternal) {
        if (tokenPayloadExternal.appIntegrity.packageName != tokenPayloadExternal.requestDetails.requestPackageName) {
            throw IntegrityException.InvalidPackageName(
                packageName = tokenPayloadExternal.appIntegrity.packageName,
                requestedPackageName = tokenPayloadExternal.requestDetails.requestPackageName,
            )
        }
    }

    private fun validateRecentActivity(recentDeviceActivity: TokenPayloadExternal.DeviceIntegrity.RecentDeviceActivity) {
        if (recentDeviceActivity.deviceActivityLevel in NOT_ALLOWED_LIST_RECENT_ACTIVITY) {
            throw IntegrityException.InvalidRecentActivity(type = recentDeviceActivity.deviceActivityLevel)
        }
    }

    private fun validateEnvironmentDetails(environmentDetails: TokenPayloadExternal.EnvironmentDetails) {
        if (environmentDetails.playProtectVerdict !in ALLOWED_ENVIRONMENT_DETAILS) {
            throw IntegrityException.InvalidEnvironmentDetails(type = environmentDetails.playProtectVerdict)
        }
    }

    private fun mapPlayIntegrityEnvelope(
        tokenPayloadExternal: TokenPayloadExternal,
        userMessage: String,
    ): PlayIntegrityEnvelope =
        PlayIntegrityEnvelope().apply {
            this.packageName = tokenPayloadExternal.requestDetails.requestPackageName
            this.requestPackageName = tokenPayloadExternal.requestDetails.requestPackageName
            this.deviceIntegrity = tokenPayloadExternal.deviceIntegrity.deviceRecognitionVerdict.firstOrNull()
            this.appIntegrity = tokenPayloadExternal.appIntegrity.appRecognitionVerdict
            this.licenseIntegrity = tokenPayloadExternal.accountDetails.appLicensingVerdict
            this.userMessage = userMessage
        }

    private fun validateResponse(tokenPayloadExternal: TokenPayloadExternal?) {
        if (tokenPayloadExternal == null) {
            throw IntegrityException.InvalidPlayIntegrity()
        }
    }

    private fun validateDeviceIntegrity(deviceIntegrity: TokenPayloadExternal.DeviceIntegrity?) {
        if (deviceIntegrity == null) {
            throw IntegrityException.InvalidDeviceIntegrity(type = "null object")
        }
        if (deviceIntegrity.deviceRecognitionVerdict.none { it in ALLOWED_DEVICE_RECOGNITION_VERDICT }) {
            throw IntegrityException.WeakDevice(
                type = "Weak Device",
            )
        }
    }

    private fun validateAppIntegrity(appIntegrity: TokenPayloadExternal.AppIntegrity?) {
        if (appIntegrity == null) throw IntegrityException.InvalidAppIntegrity(type = "null object")
        if (appIntegrity.appRecognitionVerdict.isEmpty() ||
            !appIntegrity.appRecognitionVerdict.contains("PLAY_RECOGNIZED")
        ) {
            throw IntegrityException.InvalidAppIntegrity(
                type = appIntegrity.appRecognitionVerdict,
            )
        }
    }

    private fun validateLicenseIntegrity(accountDetails: TokenPayloadExternal.AccountDetails?) {
        if (accountDetails == null) throw IntegrityException.InvalidLicenseIntegrity(type = "null object")
        if (accountDetails.appLicensingVerdict.isEmpty() ||
            !accountDetails.appLicensingVerdict.contains("LICENSED")
        ) {
            throw IntegrityException.InvalidLicenseIntegrity(
                type = accountDetails.appLicensingVerdict,
            )
        }
    }

    private fun decryptIntegrityResponse(tokenPayloadExternal: TokenPayloadExternal): String =
        runCatching {
            String(
                Base64.getDecoder().decode(
                    tokenPayloadExternal.requestDetails.nonce,
                ),
            )
        }.getOrElse {
            throw IntegrityException.InvalidNonce(nonce = tokenPayloadExternal.requestDetails.nonce)
        }

    private fun retrieveIntegrityResponse(
        token: String,
        accessToken: String,
        appleReceiptExchangeHttpClientAdapter: IntegrityHttpClient,
    ): TokenPayloadExternal {
        val headers = mapOf("Authorization" to "Bearer $accessToken")
        val body =
            mapOf(
                "integrity_token" to token,
            )
        logger().error(token)
        val response =
            appleReceiptExchangeHttpClientAdapter.post(
                url,
                headers,
                objectMapper.writeValueAsString(body),
            )
        return response.body
    }
}
