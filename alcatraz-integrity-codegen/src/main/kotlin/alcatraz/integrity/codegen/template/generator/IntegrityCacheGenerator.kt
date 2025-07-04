package alcatraz.integrity.codegen.template.generator

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.GeneratedFile
import alcatraz.codegen.GenerationConfig

class IntegrityCacheGenerator : CodeGenerator {
    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}

            import alcatraz.integrity.api.CacheService
            import com.github.benmanes.caffeine.cache.Caffeine
            import com.github.benmanes.caffeine.cache.Cache
            import org.springframework.stereotype.Service
            import java.util.UUID

            @Service
            class CacheServiceDefault : CacheService {
                val cache: Cache<UUID, String> = Caffeine.newBuilder()
                    .maximumSize(1_000_000) 
                    .build()

                override fun get(deviceId: UUID) = cache.getIfPresent(deviceId)
                override fun put(deviceId: UUID, data: String) = cache.put(deviceId, data)
                override fun remove(deviceId: UUID) = cache.invalidate(deviceId)
                override fun clearAll() = cache.cleanUp()
            }
        """.trimIndent()

        return GeneratedFile("CacheServiceDefault.kt", content, "CacheServiceDefault")
    }
}
