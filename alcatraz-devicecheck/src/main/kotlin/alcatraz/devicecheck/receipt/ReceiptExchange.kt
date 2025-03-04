package alcatraz.devicecheck.receipt

import alcatraz.devicecheck.util.Extensions.fromBase64
import alcatraz.devicecheck.util.Extensions.toBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.security.interfaces.ECPublicKey
import java.time.Instant

interface ReceiptExchange {
    val appleJwsGenerator: AppleJwsGenerator
    val receiptValidator: ReceiptValidator
    val appleReceiptExchangeHttpClientAdapter: AppleReceiptExchangeHttpClientAdapter
        get() = SimpleAppleReceiptExchangeHttpClientAdapter
    val appleDeviceCheckUrl: URI

    companion object {
        /** The Apple App Attest receipt endpoint for production use */
        @JvmStatic
        val APPLE_DEVICE_CHECK_APP_ATTEST_PRODUCTION_URL: URI =
            URI.create(
                "https://data.appattest.apple.com/v1/attestationData",
            )

        /** The Apple App Attest receipt endpoint for development use */
        @JvmStatic
        val APPLE_DEVICE_CHECK_APP_ATTEST_DEVELOPMENT_URL: URI =
            URI.create(
                "https://data-development.appattest.apple.com/v1/attestationData",
            )
    }

    suspend fun tradeAsync(
        receiptP7: ByteArray,
        attestationPublicKey: ECPublicKey,
    ): Receipt =
        coroutineScope {
            // Validate the receipt before sending it to Apple. We cannot validate the creation time as we do not know when
            // it should have been issued at latest. Therefore, we use an epoch instant which de facto skips this check. As
            // we also validate the new receipt on return, this should be acceptable.
            val receipt = receiptValidator.validateReceiptAsync(receiptP7, attestationPublicKey, Instant.EPOCH)

            val now = receiptValidator.clock.instant()
            // If the passed receipt's "not before" date has not yet passed, Apple would respond with the same receipt.
            if (receipt.payload.notBefore != null && now < receipt.payload.notBefore.value) {
                return@coroutineScope receipt
            }

            // If the passed receipt's "not after" date has already passed, Apple would not respond with a new receipt.
            val expirationDate = receipt.payload.expirationTime.value
            if (now >= expirationDate) {
                throw ReceiptExchangeException.ReceiptExpired(expirationDate)
            }

            val authorizationHeader = async { mapOf("Authorization" to appleJwsGenerator.issueToken()) }

            val response =
                withContext(Dispatchers.IO) {
                    appleReceiptExchangeHttpClientAdapter.post(
                        appleDeviceCheckUrl,
                        authorizationHeader.await(),
                        receipt.p7.toBase64().toByteArray(),
                    )
                }

            when (response.statusCode) {
                HttpURLConnection.HTTP_OK ->
                    receiptValidator.validateReceiptAsync(
                        receiptP7 = response.body.fromBase64(),
                        publicKey = attestationPublicKey,
                    )
                // Apple docs: "You made the request before the previous receipt’s “Not Before” date."
                HttpURLConnection.HTTP_NOT_MODIFIED -> receipt
                else -> {
                    handleErrorResponse(response)
                    throw ReceiptExchangeException.HttpError("Caught an error in Apple's response: $response")
                }
            }
        }

    fun trade(
        receiptP7: ByteArray,
        attestationPublicKey: ECPublicKey,
    ): Receipt =
        runBlocking {
            tradeAsync(receiptP7, attestationPublicKey)
        }

    fun handleErrorResponse(response: AppleReceiptExchangeHttpClientAdapter.Response) {}
}

class ReceiptExchangeImpl(
    override val appleJwsGenerator: AppleJwsGenerator,
    override val receiptValidator: ReceiptValidator,
    override val appleDeviceCheckUrl: URI,
) : ReceiptExchange
