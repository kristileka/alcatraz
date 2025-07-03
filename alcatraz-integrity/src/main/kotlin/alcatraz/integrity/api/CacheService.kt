package alcatraz.integrity.api

import java.util.UUID


interface CacheService {
    fun get(deviceId: UUID): String?
    fun put(deviceId: UUID, data: String)
    fun remove(deviceId: UUID)
    fun clearAll()
}