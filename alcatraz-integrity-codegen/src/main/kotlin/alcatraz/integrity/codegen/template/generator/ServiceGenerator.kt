package alcatraz.integrity.codegen.template.generator

import alcatraz.integrity.codegen.CodeGenerator
import alcatraz.integrity.codegen.GeneratedFile
import alcatraz.integrity.codegen.GenerationConfig
import java.time.LocalDateTime

class ServiceGenerator : CodeGenerator {
    override fun generate(config: GenerationConfig): GeneratedFile {
        val content = """
            package ${config.packageName}

            import org.springframework.stereotype.Service
            import java.time.LocalDateTime

            @Service
            class CustomerService1 {
                
                fun getCustomer(): String {
                    return "Customer data retrieved at ${LocalDateTime.now()}"
                }
                
                fun getCustomerById(id: Long): String {
                    return "Customer with ID"
                }
                
                fun getAllCustomers(): List<String> {
                    return listOf("Customer1", "Customer2", "Customer3")
                }
            }
        """.trimIndent()

        return GeneratedFile("CustomerService1.kt", content, "CustomerService1")
    }
}