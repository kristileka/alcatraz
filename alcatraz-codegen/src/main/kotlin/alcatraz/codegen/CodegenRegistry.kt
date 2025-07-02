package alcatraz.codegen

interface CodegenRegistry {
    val generators: MutableMap<String, CodeGenerator>

    fun getGenerator(name: String): CodeGenerator? = generators[name]

    fun getAllGenerators(): Map<String, CodeGenerator> = generators.toMap()

    fun getAvailableGenerators(config: GenerationConfig): Map<String, CodeGenerator> {
        return generators.filter { (_, generator) -> generator.canGenerate(config) }
    }

    fun register(name: String, generator: CodeGenerator) {
        generators[name] = generator
    }
}