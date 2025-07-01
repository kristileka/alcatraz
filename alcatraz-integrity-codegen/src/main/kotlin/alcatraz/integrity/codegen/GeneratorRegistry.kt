package alcatraz.integrity.codegen

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.CodegenRegistry
import alcatraz.integrity.codegen.template.generator.ControllerGenerator
import alcatraz.integrity.codegen.template.generator.DeviceCheckGenerator
import alcatraz.integrity.codegen.template.generator.ServiceGenerator

class IntegrityRegistry : CodegenRegistry {
    override val generators = mutableMapOf<String, CodeGenerator>()

    init {
        register("controller", ControllerGenerator())
        register("service", ServiceGenerator())
        register("devicecheck", DeviceCheckGenerator())
    }

    fun register(name: String, generator: CodeGenerator) {
        generators[name] = generator
    }
}