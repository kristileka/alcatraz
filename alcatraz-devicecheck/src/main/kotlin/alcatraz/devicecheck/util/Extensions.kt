package alcatraz.devicecheck.util

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.security.PublicKey
import java.security.cert.CertPathValidator
import java.security.cert.CertificateFactory
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.util.Date
import java.util.UUID

@Suppress("TooManyFunctions")
internal object Extensions {
    fun List<X509Certificate>.verifyChain(
        trustAnchor: TrustAnchor,
        date: Date,
    ) {
        val certFactory = CertificateFactory.getInstance("X509")
        val certPath = certFactory.generateCertPath(this)
        val certPathValidator = CertPathValidator.getInstance("PKIX")
        val pkixParameters =
            PKIXParameters(setOf(trustAnchor)).apply {
                isRevocationEnabled = false
                this.date = date
            }

        certPathValidator.validate(certPath, pkixParameters)
    }

    fun SubjectPublicKeyInfo.createAppleKeyId() = publicKeyData.bytes.sha256()

    fun PublicKey.createAppleKeyId() = SubjectPublicKeyInfo.getInstance(encoded).createAppleKeyId()

    fun X509CertificateHolder.createAppleKeyId() = subjectPublicKeyInfo.createAppleKeyId()

    fun X509Certificate.createAppleKeyId() = publicKey.createAppleKeyId()

    inline operator fun <reified T : Any> ASN1Sequence.get(index: Int): T = this.getObjectAt(index) as T

    inline fun <reified T : Any> ASN1InputStream.readObjectAs(): T = this.readObject() as T

    fun ByteArray.sha256(): ByteArray = MessageDigest.getInstance("SHA-256").digest(this)

    fun ByteArray.toBase64(): String = Base64.toBase64String(this)

    fun ByteArray.fromBase64(): ByteArray = Base64.decode(this)

    fun String.fromBase64(): ByteArray = Base64.decode(this)

    @Suppress("MagicNumber")
    fun ByteArray.readAsUInt16(): Int {
        require(this.size == 2) { "Expected an unsigned 2 byte integer" }
        return ByteBuffer
            .wrap(ByteArray(2) + this)
            .order(ByteOrder.BIG_ENDIAN)
            .int
    }

    @Suppress("MagicNumber")
    fun ByteArray.readAsUInt32(): Long {
        require(this.size == 4) { "Expected an unsigned 4 byte integer" }
        return ByteBuffer
            .wrap(ByteArray(4) + this)
            .order(ByteOrder.BIG_ENDIAN)
            .long
    }

    /**
     * Create a UUID from a [ByteArray] of a length no more than 16 bytes, padded with zeros, if necessary.
     */
    @Suppress("MagicNumber")
    fun ByteArray.toUUID(): UUID {
        require(this.size <= 16) { "Byte array must not contain more than 16 bytes" }
        return ByteArray(16)
            .let { this.copyInto(it, 0) }
            .let(ByteBuffer::wrap)
            .let { UUID(it.long, it.long) }
    }

    object Pkcs7 {
        fun ByteArray.readAsSignedData(): CMSSignedData =
            ASN1InputStream(this).use {
                it.readObject().let(ContentInfo::getInstance).let(::CMSSignedData)
            }

        fun CMSSignedData.readCertificateChain(): List<X509Certificate> =
            this.certificates.getMatches(null).map {
                JcaX509CertificateConverter().getCertificate(it)
            }
    }
}
