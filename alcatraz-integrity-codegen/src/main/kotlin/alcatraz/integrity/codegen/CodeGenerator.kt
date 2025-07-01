package alcatraz.integrity.codegen

interface CodeGenerator {
    fun generate(config: GenerationConfig): GeneratedFile
    fun canGenerate(config: GenerationConfig): Boolean = true
}
