package alcatraz.integrity.codegen.template.generator

import alcatraz.integrity.codegen.CodeGenerator
import alcatraz.integrity.codegen.GeneratedFile
import alcatraz.integrity.codegen.GenerationConfig

class ControllerGenerator : CodeGenerator {
    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}

            import org.springframework.web.bind.annotation.GetMapping
            import org.springframework.web.bind.annotation.RequestMapping
            import org.springframework.web.bind.annotation.RestController

            @RestController
            @RequestMapping("${config.basePath}1")
            class CustomerController1(
                private val customerService: CustomerService
            ) {

                @GetMapping("/test")
                fun getTest(): String {
                    return customerService.getCustomer()
                }
                
                @GetMapping("/status")
                fun getStatus(): Map<String, Any> {
                    return mapOf(
                        "status" to "active",
                        "timestamp" to System.currentTimeMillis()
                    )
                }
            }
        """.trimIndent()

        return GeneratedFile("CustomerController1.kt", content, "CustomerController1")
    }
}
