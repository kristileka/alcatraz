package alcatraz.integrity.codegen

import alcatraz.codegen.CodeGenerator
import alcatraz.codegen.CodegenRegistry
import alcatraz.integrity.codegen.template.generator.IntegrityCacheGenerator
import alcatraz.integrity.codegen.template.generator.IntegrityControllerGenerator
import alcatraz.integrity.codegen.template.generator.IntegrityConfigurationGenerator

class IntegrityRegistry : CodegenRegistry {
    override val generators = mutableMapOf<String, CodeGenerator>()

    init {
        register("controller", IntegrityControllerGenerator())
        register("configuration", IntegrityConfigurationGenerator())
        register("cache", IntegrityCacheGenerator())
    }
}