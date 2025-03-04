package alcatraz.devicecheck.attestation

import alcatraz.devicecheck.receipt.Receipt
import java.security.cert.X509Certificate

data class ValidatedAttestation(
    val certificate: X509Certificate,
    val receipt: Receipt,
    val iOSVersion: String?,
)
