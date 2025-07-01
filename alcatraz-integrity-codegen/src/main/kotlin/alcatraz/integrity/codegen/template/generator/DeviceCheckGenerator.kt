package alcatraz.integrity.codegen.template.generator

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.GeneratedFile
import alcatraz.codegen.GenerationConfig

class DeviceCheckGenerator : CodeGenerator {
    override fun canGenerate(config: GenerationConfig): Boolean {
        return !config.teamIdentifier.isNullOrBlank()
    }

    override fun generate(config: GenerationConfig): GeneratedFile {
        val teamId = config.teamIdentifier ?: throw IllegalStateException("Team identifier is required")

        val content = """
            package ${config.packageName}
        """.trimIndent()

        return GeneratedFile("DeviceCheckService1.kt", content, "DeviceCheckService1")
    }
}
