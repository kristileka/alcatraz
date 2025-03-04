package alcatraz.devicecheck.common

import alcatraz.devicecheck.util.Extensions.toUUID
import java.util.UUID

/**
 * The environment for an app that uses the App Attest service to validate itself.
 *
 * @property identifier An App Attest–specific constant that indicates whether the attested key belongs to the
 *   development or production environment.
 */
enum class AppleAppAttestEnvironment(
    private val identifier: String,
) {
    DEVELOPMENT("appattestdevelop"),
    PRODUCTION("appattest"),
    ;

    val aaguid: UUID = identifier.toByteArray().toUUID()
}
