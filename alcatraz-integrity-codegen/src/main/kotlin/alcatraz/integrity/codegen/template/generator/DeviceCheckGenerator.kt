package alcatraz.integrity.codegen.template.generator

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.GeneratedFile
import alcatraz.codegen.GenerationConfig

class DeviceCheckGenerator : CodeGenerator {
    override fun canGenerate(config: GenerationConfig): Boolean {
        return !config.teamIdentifier.isNullOrBlank()
    }

    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}
        """.trimIndent()

        return GeneratedFile("DeviceCheckService1.kt", content, "DeviceCheckService1")
    }
}
