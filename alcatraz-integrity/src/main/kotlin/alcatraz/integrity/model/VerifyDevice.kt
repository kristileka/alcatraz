package alcatraz.integrity.model

import java.util.UUID

data class VerifyDevice(
    val deviceId: UUID,
    val token: String
)
