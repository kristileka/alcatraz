package alcatraz.codegen

interface CodeGenerator {
    fun generate(config: GenerationConfig): GeneratedFile
    fun canGenerate(config: GenerationConfig): Boolean = true
}
