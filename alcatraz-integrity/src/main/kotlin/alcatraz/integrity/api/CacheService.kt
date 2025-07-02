package alcatraz.integrity.api


interface CacheService {
    fun get(deviceId: String): String?
    fun put(deviceId: String, data: String)
    fun remove(deviceId: String)
    fun clearAll()
}