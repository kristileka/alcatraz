package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension

class ModuleRegistry {

    private val modules = mapOf(
        "devicecheck" to CodegenModule(
            projectName = ":alcatraz-devicecheck-codegen",
            mainClass = "alcatraz.devicecheck.codegen.GenerateCommandKt",
            description = "DeviceCheck integration classes"
        ),
        "integrity" to CodegenModule(
            projectName = ":alcatraz-integrity-codegen",
            mainClass = "alcatraz.integrity.codegen.cli.GenerateCommandKt",
            description = "Integrity verification classes"
        )
    )
    
    fun getAllModules(): Map<String, CodegenModule> = modules

    fun getEnabledModules(extension: AlcatrazExtension): List<String> {
        return modules.keys.filter { moduleName ->
            when (moduleName) {
                "devicecheck" -> extension.getDeviceCheck().enabled.getOrElse(false)
                "integrity" -> extension.getIntegrity().enabled.getOrElse(false)
                else -> true
            }
        }
    }
    
    fun isModuleEnabled(extension: AlcatrazExtension, moduleName: String): Boolean {
        return when (moduleName) {
            "devicecheck" -> extension.getDeviceCheck().enabled.getOrElse(false)
            "integrity" -> extension.getIntegrity().enabled.getOrElse(false)
            else -> true
        }
    }
}
