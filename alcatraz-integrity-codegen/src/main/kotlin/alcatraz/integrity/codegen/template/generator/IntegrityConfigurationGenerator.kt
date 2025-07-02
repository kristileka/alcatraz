package alcatraz.integrity.codegen.template.generator

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.GeneratedFile
import alcatraz.codegen.GenerationConfig

class IntegrityConfigurationGenerator : CodeGenerator {
    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}

            import org.springframework.context.annotation.Configuration
            import org.springframework.context.annotation.Bean
            import alcatraz.integrity.factory.PlayIntegrityServiceFactory
            import alcatraz.integrity.api.PlayIntegrityService
            
            @Configuration
            class IntegrityConfiguration {
               
                @Bean
                fun provideIntegrityService(): PlayIntegrityService {
                    return PlayIntegrityServiceFactory.create("${config.packageName}"){
                        "googleToken"
                    }
                }
            }
        """.trimIndent()

        return GeneratedFile("IntegrityConfiguration.kt", content, "IntegrityConfiguration")
    }
}