package alcatraz.integrity.codegen.template.generator

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.GeneratedFile
import alcatraz.codegen.GenerationConfig

class IntegrityControllerGenerator : CodeGenerator {
    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}

            import org.springframework.web.bind.annotation.GetMapping
            import org.springframework.web.bind.annotation.RequestMapping
            import org.springframework.web.bind.annotation.RestController
            import alcatraz.integrity.api.PlayIntegrityService
            import alcatraz.integrity.model.PlayIntegrityEnvelope
            import alcatraz.integrity.model.RegisterDevice
            import alcatraz.integrity.provider.PlayIntegrityProvider
            import org.springframework.web.bind.annotation.PostMapping
            import org.springframework.web.bind.annotation.RequestBody

            @RestController
            @RequestMapping("${config.basePath}")
            class IntegrityController(
                private val integrityService: PlayIntegrityService,
                private val integrityProvider: PlayIntegrityProvider
            ) {

                @PostMapping("/challenge")
                fun registerChallenge(
                  @RequestBody registerDevice: RegisterDevice
                ): String {
                    return integrityProvider.getChallenge(registerDevice.deviceId)
                }
                
                @GetMapping("/test")
                fun getTest(): PlayIntegrityEnvelope {
                    return integrityService.validate("Asd")
                }
            }
        """.trimIndent()

        return GeneratedFile("IntegrityController.kt", content, "IntegrityController")
    }
}
