package alcatraz.devicecheck.codegen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

class GenerateCommand : CliktCommand(name = "generate", help = "Generate code based on configuration") {
    private val packageName by option(
        "-p",
        "--package",
        help = "The package name for generated classes",
    ).required()

    private val outputDir by option(
        "-o",
        "--output",
        help = "The output directory where classes will be generated",
    ).file(canBeFile = false, mustExist = false)
    private val teamIdentifier by option(
        "-t",
        "--team-id",
        help = "Apple DeviceCheck team identifier",
    )

    override fun run() {
        // Create output directory if it doesn't exist
        val outDir = outputDir ?: File("build/generated/alcatraz")
        val packageDir = File(outDir, packageName.replace('.', '/'))
        packageDir.mkdirs()

        // Generate the controller class
        val controllerContent =
            """
            package $packageName
            
            import org.springframework.web.bind.annotation.GetMapping
            import org.springframework.web.bind.annotation.RequestMapping
            import org.springframework.web.bind.annotation.RestController
            
            @RestController
            @RequestMapping("alcatraz")
            class CustomerController(
                private val customerService: CustomerService
            ) {
            
                @GetMapping("/test")
                fun getTest(): String {
                    return customerService.getCustomer()
                }
            }
            """.trimIndent()

        File(packageDir, "CustomerController.kt").writeText(controllerContent)

        // Also generate a basic service class
        val serviceContent =
            """
            package $packageName
            
            import org.springframework.stereotype.Service
            
            @Service
            class CustomerService {
                fun getCustomer(): String {
                    return "Customer data"
                }
            }
            """.trimIndent()

        File(packageDir, "CustomerService.kt").writeText(serviceContent)

        // Generate DeviceCheck service if team ID is provided
        if (!teamIdentifier.isNullOrBlank()) {
            val deviceCheckContent =
                """
                package $packageName
                
                import org.springframework.stereotype.Component
                
                @Component
                class DeviceCheckService {
                    val teamIdentifier = "$teamIdentifier"
                    
                    fun verifyDevice(token: String): Boolean {
                        // Implementation would use Apple's DeviceCheck API with the teamIdentifier
                        return true
                    }
                }
                """.trimIndent()

            File(packageDir, "DeviceCheckService.kt").writeText(deviceCheckContent)
        }

        echo("Generated classes in ${packageDir.absolutePath}")
    }
}

fun main(args: Array<String>) {
    GenerateCommand().main(args)
}
