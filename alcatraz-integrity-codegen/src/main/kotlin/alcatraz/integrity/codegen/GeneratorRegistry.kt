package alcatraz.integrity.codegen

import alcatraz.integrity.codegen.template.generator.ControllerGenerator
import alcatraz.integrity.codegen.template.generator.DeviceCheckGenerator
import alcatraz.integrity.codegen.template.generator.ServiceGenerator

class GeneratorRegistry {
    private val generators = mutableMapOf<String, CodeGenerator>()

    init {
        register("controller", ControllerGenerator())
        register("service", ServiceGenerator())
        register("devicecheck", DeviceCheckGenerator())
    }

    fun register(name: String, generator: CodeGenerator) {
        generators[name] = generator
    }

    fun getGenerator(name: String): CodeGenerator? = generators[name]

    fun getAllGenerators(): Map<String, CodeGenerator> = generators.toMap()

    fun getAvailableGenerators(config: GenerationConfig): Map<String, CodeGenerator> {
        return generators.filter { (_, generator) -> generator.canGenerate(config) }
    }
}