package alcatraz.devicecheck.assertion

import alcatraz.devicecheck.common.AuthenticatorData
import alcatraz.devicecheck.common.AuthenticatorDataFlag
import alcatraz.devicecheck.util.Extensions.sha256
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.security.Signature
import java.security.interfaces.ECPublicKey
import kotlin.experimental.and
import kotlin.experimental.xor

interface AssertionValidator {
    val appIdentifier: String
    val assertionChallengeValidator: AssertionChallengeValidator

    companion object {
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }

    fun validate(
        assertionObject: ByteArray,
        clientData: ByteArray,
        attestationPublicKey: ECPublicKey,
        lastCounter: Long,
        challenge: ByteArray,
    ): Assertion =
        runBlocking {
            validateAsync(assertionObject, clientData, attestationPublicKey, lastCounter, challenge)
        }

    suspend fun validateAsync(
        assertionObject: ByteArray,
        clientData: ByteArray,
        attestationPublicKey: ECPublicKey,
        lastCounter: Long,
        challenge: ByteArray,
    ): Assertion
}

class AssertionValidatorImpl(
    override val appIdentifier: String,
    override val assertionChallengeValidator: AssertionChallengeValidator,
) : AssertionValidator {
    private val cborObjectReader =
        ObjectMapper(CBORFactory())
            .registerKotlinModule()
            .readerFor(AssertionEnvelope::class.java)

    private fun verifySignature(
        assertionEnvelope: AssertionEnvelope,
        clientData: ByteArray,
        attestationPublicKey: ECPublicKey,
    ) {
        val clientDataHash = clientData.sha256()

        val nonce = assertionEnvelope.authenticatorData.plus(clientDataHash).sha256()

        val signatureInstance = Signature.getInstance(AssertionValidator.SIGNATURE_ALGORITHM)
        runCatching {
            signatureInstance.run {
                initVerify(attestationPublicKey)
                update(nonce)
                verify(assertionEnvelope.signature)
            }
        }.onFailure { cause ->
            throw AssertionException.InvalidSignature(cause)
        }.onSuccess { valid ->
            if (!valid) {
                throw AssertionException.InvalidSignature()
            }
        }
    }

    @Suppress("ThrowsCount")
    private fun verifyAuthenticatorData(
        authenticatorDataBlob: ByteArray,
        lastCounter: Long,
    ): AuthenticatorData {
        authenticatorDataBlob[AuthenticatorData.FLAGS_INDEX] =
            authenticatorDataBlob[AuthenticatorData.FLAGS_INDEX]
                .and(AuthenticatorDataFlag.ED.bitmask.xor(1))
                .and(AuthenticatorDataFlag.AT.bitmask.xor(1))

        val authenticatorData =
            runCatching { AuthenticatorData.parse(authenticatorDataBlob) }
                .getOrElse {
                    throw AssertionException.InvalidAuthenticatorData("Could not parse assertion authenticatorData")
                }
        val expectedRpId = appIdentifier.toByteArray().sha256()
        if (!expectedRpId.contentEquals(authenticatorData.rpIdHash)) {
            throw AssertionException.InvalidAuthenticatorData("App ID hash does not match RP ID hash")
        }

        if (authenticatorData.signCount <= lastCounter) {
            throw AssertionException.InvalidAuthenticatorData(
                "Assertion counter is not greater than the counter saved counter",
            )
        }

        return authenticatorData
    }

    private fun verifyChallenge(
        challenge: ByteArray,
        assertionObj: Assertion,
        clientData: ByteArray,
        attestationPublicKey: ECPublicKey,
    ) {
        val challengeIsValid =
            assertionChallengeValidator.validate(
                assertionObj = assertionObj,
                clientData = clientData,
                attestationPublicKey = attestationPublicKey,
                challenge = challenge,
            )

        if (!challengeIsValid) {
            throw AssertionException.InvalidChallenge("The given challenge is invalid")
        }
    }

    override suspend fun validateAsync(
        assertionObject: ByteArray,
        clientData: ByteArray,
        attestationPublicKey: ECPublicKey,
        lastCounter: Long,
        challenge: ByteArray,
    ): Assertion =
        coroutineScope {
            val assertionEnvelope = cborObjectReader.readValue<AssertionEnvelope>(assertionObject)

            launch { verifySignature(assertionEnvelope, clientData, attestationPublicKey) }

            val authenticatorData =
                async {
                    verifyAuthenticatorData(assertionEnvelope.authenticatorData, lastCounter)
                }

            val assertion = Assertion(assertionEnvelope.signature, authenticatorData.await())

            launch { verifyChallenge(challenge, assertion, clientData, attestationPublicKey) }

            assertion
        }
}
