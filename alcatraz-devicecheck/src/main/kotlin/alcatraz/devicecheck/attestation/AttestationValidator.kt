package alcatraz.devicecheck.attestation

import alcatraz.devicecheck.common.AppleAppAttestEnvironment
import alcatraz.devicecheck.common.AuthenticatorData
import alcatraz.devicecheck.receipt.Receipt
import alcatraz.devicecheck.receipt.ReceiptException
import alcatraz.devicecheck.receipt.ReceiptValidator
import alcatraz.devicecheck.util.Extensions.createAppleKeyId
import alcatraz.devicecheck.util.Extensions.fromBase64
import alcatraz.devicecheck.util.Extensions.readObjectAs
import alcatraz.devicecheck.util.Extensions.sha256
import alcatraz.devicecheck.util.Extensions.verifyChain
import alcatraz.devicecheck.util.Utils
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DLSequence
import org.bouncycastle.asn1.DLTaggedObject
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.Arrays.constantTimeAreEqual
import java.security.GeneralSecurityException
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.time.Clock
import java.util.Date

/**
 * Interface to validate the authenticity of an Apple App Attest attestation.
 *
 * @property app The connecting app.
 * @property appleAppAttestEnvironment The Apple App Attest environment; either "appattestdevelop" or "appattest".
 * @property trustAnchor The root of the App Attest certificate chain.
 * @property receiptValidator A [ReceiptValidator] to validate the receipt contained in the attestation statement.
 * @property clock A clock instance. Defaults to the system clock. Should be only relevant for testing.
 */
interface AttestationValidator {
    val appIdentifier: String
    val appleAppAttestEnvironment: AppleAppAttestEnvironment
    val trustAnchor: TrustAnchor
        get() = APPLE_APP_ATTEST_ROOT_CA_BUILTIN_TRUST_ANCHOR
    val receiptValidator: ReceiptValidator
    val clock: Clock

    /** Constants for Apple's X.509 extension object identifiers */
    object AppleCertificateExtensions {
        /** Identifier for the object which contains the nonce the app includes in the attestation call */
        const val NONCE_OID = "1.2.840.113635.100.8.2"

        /** The tag number of the nonce parent object */
        const val NONCE_TAG_NO = 1

        /** Identifier for the object which contains the iOS version the app being attested is running */
        const val OS_VERSION_OID = "1.2.840.113635.100.8.7"

        /** The tag number of the iOS version parent object */
        const val OS_VERSION_TAG_NO = 1400
    }

    companion object {
        /** The root certificate authority of the attestation certificate */
        @JvmField
        val APPLE_APP_ATTEST_ROOT_CA_BUILTIN_TRUST_ANCHOR =
            TrustAnchor(
                Utils.readPemX509Certificate(
                    """
                    -----BEGIN CERTIFICATE-----
                    MIICITCCAaegAwIBAgIQC/O+DvHN0uD7jG5yH2IXmDAKBggqhkjOPQQDAzBSMSYw
                    JAYDVQQDDB1BcHBsZSBBcHAgQXR0ZXN0YXRpb24gUm9vdCBDQTETMBEGA1UECgwK
                    QXBwbGUgSW5jLjETMBEGA1UECAwKQ2FsaWZvcm5pYTAeFw0yMDAzMTgxODMyNTNa
                    Fw00NTAzMTUwMDAwMDBaMFIxJjAkBgNVBAMMHUFwcGxlIEFwcCBBdHRlc3RhdGlv
                    biBSb290IENBMRMwEQYDVQQKDApBcHBsZSBJbmMuMRMwEQYDVQQIDApDYWxpZm9y
                    bmlhMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAERTHhmLW07ATaFQIEVwTtT4dyctdh
                    NbJhFs/Ii2FdCgAHGbpphY3+d8qjuDngIN3WVhQUBHAoMeQ/cLiP1sOUtgjqK9au
                    Yen1mMEvRq9Sk3Jm5X8U62H+xTD3FE9TgS41o0IwQDAPBgNVHRMBAf8EBTADAQH/
                    MB0GA1UdDgQWBBSskRBTM72+aEH/pwyp5frq5eWKoTAOBgNVHQ8BAf8EBAMCAQYw
                    CgYIKoZIzj0EAwMDaAAwZQIwQgFGnByvsiVbpTKwSga0kP0e8EeDS4+sQmTvb7vn
                    53O5+FRXgeLhpJ06ysC5PrOyAjEAp5U4xDgEgllF7En3VcE3iexZZtKeYnpqtijV
                    oyFraWVIyd/dganmrduC1bmTBGwD
                    -----END CERTIFICATE-----
                    """.trimIndent(),
                ),
                null,
            )
    }

    /**
     * Validate an attestation object.
     *
     * @param attestationObject attestation object created by calling
     *   `DCAppAttestService.attestKey(_:clientDataHash:completionHandler:)`
     * @param keyIdBase64 Base64-encoded key identifier which was created when calling
     *   `DCAppAttestService.generateKey(completionHandler:)`
     * @param serverChallenge The one-time challenge the server created. The iOS app incorporates a hash of this
     *   challenge in the call to `DCAppAttestService.attestKey(_:clientDataHash:completionHandler:)`
     *
     * @throws AttestationException If any attestation validation error occurs, an [AttestationException] is thrown.
     *
     * @return A [ValidatedAttestation] object for the given [attestationObject].
     */
    fun validate(
        attestationObject: ByteArray,
        keyIdBase64: String,
        serverChallenge: ByteArray,
    ): ValidatedAttestation =
        runBlocking {
            validateAsync(attestationObject, keyIdBase64, serverChallenge)
        }

    /**
     * Validate an attestation object. Suspending version of [validate].
     *
     * @see validate
     */
    suspend fun validateAsync(
        attestationObject: ByteArray,
        keyIdBase64: String,
        serverChallenge: ByteArray,
    ): ValidatedAttestation
}

/**
 * Implementation of [AttestationValidator].
 *
 * @throws AttestationException
 */
@Suppress("TooManyFunctions")
class AttestationValidatorImpl(
    override val appIdentifier: String,
    override val appleAppAttestEnvironment: AppleAppAttestEnvironment,
    override val clock: Clock,
    override val receiptValidator: ReceiptValidator,
) : AttestationValidator {
    private val cborObjectReader =
        ObjectMapper(CBORFactory())
            .registerKotlinModule()
            .readerFor(AttestationObject::class.java)

    override suspend fun validateAsync(
        attestationObject: ByteArray,
        keyIdBase64: String,
        serverChallenge: ByteArray,
    ): ValidatedAttestation =
        coroutineScope {
            val attestation = parseAttestationObject(attestationObject)
            val keyId = keyIdBase64.fromBase64()

            launch { verifyAttestationFormat(attestation) }
            launch { verifyCertificateChain(attestation) }
            launch { verifyNonce(attestation, serverChallenge) }
            val credCert = verifyAttestationCertificate(attestation, keyId)
            launch { verifyAuthenticatorData(attestation, keyId) }
            val receipt = validateAttestationReceiptAsync(attestation)
            val iOSVersion = parseIOSVersion(credCert)

            ValidatedAttestation(
                certificate = credCert,
                receipt = receipt,
                iOSVersion = iOSVersion,
            )
        }

    private fun parseAttestationObject(attestationObject: ByteArray): AttestationObject = cborObjectReader.readValue(attestationObject)

    private fun verifyAttestationFormat(attestationObject: AttestationObject) {
        if (attestationObject.fmt != AttestationObject.APPLE_APP_ATTEST_ATTESTATION_STATEMENT_FORMAT_IDENTIFIER) {
            throw AttestationException.InvalidFormatException(
                "Expected `${AttestationObject.APPLE_APP_ATTEST_ATTESTATION_STATEMENT_FORMAT_IDENTIFIER}` " +
                    "but was ${attestationObject.fmt}",
            )
        }
    }

    private fun verifyCertificateChain(attestationObject: AttestationObject) {
        // 1. Verify that the x5c array contains the intermediate and leaf certificates for App Attest,
        //    starting from the credential certificate stored in the first data buffer in the array (credcert).
        //    Verify the validity of the certificates using Apple’s App Attest root certificate.
        val certs = attestationObject.attStmt.x5c.map { Utils.readDerX509Certificate(it) }
        try {
            certs.verifyChain(trustAnchor, date = Date.from(clock.instant()))
        } catch (ex: GeneralSecurityException) {
            throw AttestationException.InvalidCertificateChain(
                "The attestation object does not contain a valid certificate chain",
                ex,
            )
        }
    }

    private fun extractNonce(credCertDer: ByteArray): ByteArray {
        val credCert = Utils.readDerX509Certificate(credCertDer)
        val octetString =
            getTaggedOctetString(
                credCert = credCert,
                oid = AttestationValidator.AppleCertificateExtensions.NONCE_OID,
                tagNo = AttestationValidator.AppleCertificateExtensions.NONCE_TAG_NO,
            )
        return octetString.octets
    }

    private fun verifyNonce(
        attestationObject: AttestationObject,
        serverChallenge: ByteArray,
    ) {
        // 2. Create clientDataHash as the SHA256 hash of the one-time challenge sent to your app before performing
        //    the attestation, ...
        val clientDataHash = serverChallenge.sha256()

        //    ... and append that hash to the end of the authenticator data (authData from the decoded object).
        // 3. Generate a new SHA256 hash of the composite item to create nonce.
        val expectedNonce = attestationObject.authData.plus(clientDataHash).sha256()

        // 4. Obtain the value of the credCert extension with OID 1.2.840.113635.100.8.2, which is a DER-encoded
        //    ASN.1 sequence. Decode the sequence and extract the single octet string that it contains ...
        val actualNonce =
            kotlin
                .runCatching {
                    extractNonce(attestationObject.attStmt.x5c.first())
                }.getOrElse {
                    throw AttestationException.InvalidNonce(it)
                }

        //   ... Verify that the string equals nonce.
        if (!constantTimeAreEqual(expectedNonce, actualNonce)) {
            throw AttestationException.InvalidNonce()
        }
    }

    private fun verifyAttestationCertificate(
        attestationObject: AttestationObject,
        keyId: ByteArray,
    ): X509Certificate {
        // 5. Create the SHA256 hash of the public key in credCert, ...
        val credCertHolder = X509CertificateHolder(attestationObject.attStmt.x5c.first())
        val actualKeyId = credCertHolder.createAppleKeyId()

        //    ... and verify that it matches the key identifier from your app.
        if (!actualKeyId.contentEquals(keyId)) {
            throw AttestationException.InvalidPublicKey(keyId)
        }

        return JcaX509CertificateConverter()
            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .getCertificate(credCertHolder)
    }

    @Suppress("ThrowsCount")
    private fun verifyAuthenticatorData(
        attestationObject: AttestationObject,
        keyId: ByteArray,
    ) {
        val authenticatorData = AuthenticatorData.parse(attestationObject.authData)

        if (authenticatorData.attestedCredentialData == null) {
            throw AttestationException.InvalidAuthenticatorData("Does not contain attested credentials")
        }

        // 6. Compute the SHA256 hash of your app’s App ID, and verify that this is the same as the authenticator
        //    data’s RP ID hash.
        if (!authenticatorData.rpIdHash.contentEquals(appIdentifier.toByteArray().sha256())) {
            throw AttestationException.InvalidAuthenticatorData("App ID does not match RP ID hash")
        }

        // 7. Verify that the authenticator data’s counter field equals 0.
        if (authenticatorData.signCount != 0L) {
            throw AttestationException.InvalidAuthenticatorData("Counter is not zero")
        }

        // 8. Verify that the authenticator data’s aaguid field is either appattestdevelop if operating in the
        //    development environment, or appattest followed by seven 0x00 bytes if operating in the production
        //    environment.
        if (authenticatorData.attestedCredentialData.aaguid != appleAppAttestEnvironment.aaguid) {
            throw AttestationException.InvalidAuthenticatorData(
                "AAGUID does match neither ${AppleAppAttestEnvironment.DEVELOPMENT} " +
                    "nor ${AppleAppAttestEnvironment.PRODUCTION}",
            )
        }

        // 9. Verify that the authenticator data’s credentialId field is the same as the key identifier.
        if (!authenticatorData.attestedCredentialData.credentialId.contentEquals(keyId)) {
            throw AttestationException.InvalidAuthenticatorData("Credentials ID is not equal to Key ID")
        }
    }

    private suspend fun validateAttestationReceiptAsync(attestStatement: AttestationObject): Receipt {
        val receiptP7 = attestStatement.attStmt.receipt
        val attestationCertificate =
            attestStatement.attStmt.x5c
                .first()
                .let(Utils::readDerX509Certificate)
        val publicKey = attestationCertificate.publicKey as ECPublicKey

        return try {
            receiptValidator.validateReceiptAsync(receiptP7, publicKey)
        } catch (ex: ReceiptException) {
            throw AttestationException.InvalidReceipt(ex)
        }
    }

    /**
     * Reads the octet string of an X.509 extension value in the following format:
     *
     *      OCTET STRING
     *          SEQUENCE
     *              [1] (TAGGED OBJECT)
     *                  OCTET STRING
     */
    private fun getTaggedOctetString(
        credCert: X509Certificate,
        oid: String,
        tagNo: Int,
    ): DEROctetString {
        val value = credCert.getExtensionValue(oid)
        val envelope = ASN1InputStream(value).readObjectAs<DEROctetString>()
        val sequence = ASN1InputStream(envelope.octetStream).readObjectAs<DLSequence>()
        val taggedObject = sequence.first { (it is DLTaggedObject) && it.tagNo == tagNo } as DLTaggedObject
        return taggedObject.baseObject as DEROctetString
    }

    private fun parseIOSVersion(credCert: X509Certificate): String? =
        runCatching {
            getTaggedOctetString(
                credCert = credCert,
                oid = AttestationValidator.AppleCertificateExtensions.OS_VERSION_OID,
                tagNo = AttestationValidator.AppleCertificateExtensions.OS_VERSION_TAG_NO,
            ).octets.let(::String)
        }.getOrNull()
}
