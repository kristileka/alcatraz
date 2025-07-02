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

            @Service
            class CacheServiceDefault : CacheService {
                val cache: Cache<String, String> = Caffeine.newBuilder()
                    .maximumSize(1_000_000) 
                    .build()

                override fun get(deviceId: String) = cache.getIfPresent(deviceId)
                override fun put(deviceId: String, data: String) = cache.put(deviceId, data)
                override fun remove(deviceId: String) = cache.invalidate(deviceId)
                override fun clearAll() = cache.cleanUp()
            }
        """.trimIndent()

        return GeneratedFile("CacheServiceDefault.kt", content, "CacheServiceDefault")
    }
}
