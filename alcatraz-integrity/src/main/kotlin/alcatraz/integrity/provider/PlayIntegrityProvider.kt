package alcatraz.integrity.provider

import alcatraz.integrity.api.CacheService
import java.util.UUID

class PlayIntegrityProvider(
    private val cacheService: CacheService
) {
    fun getChallenge(deviceId: UUID): String {
        val challenge = cacheService.get(deviceId)
            ?: UUID.randomUUID().toString()
        cacheService.put(deviceId, challenge)
        return challenge
    }
}