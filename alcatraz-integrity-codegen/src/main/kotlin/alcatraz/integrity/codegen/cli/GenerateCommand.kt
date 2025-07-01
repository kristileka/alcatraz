package alcatraz.integrity.codegen.cli

import alcatraz.integrity.codegen.GenerationConfig
import alcatraz.integrity.codegen.GeneratorRegistry
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.default
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
        "--token",
        help = "Google token",
    )

    private val basePath by option(
        "-b",
        "--base-path",
        help = "Base path for REST endpoints"
    ).default("alcatraz")

    override fun run() {
        val outDir = outputDir ?: File("build/generated/alcatraz")
        val config = GenerationConfig(
            packageName = packageName,
            outputDir = outDir,
            teamIdentifier = teamIdentifier,
            basePath = basePath
        )

        val registry = GeneratorRegistry()

        val requestedModules = registry.getAvailableGenerators(config).keys

        echo("Generating modules: ${requestedModules.joinToString(", ")}")
        echo("Output directory: ${outDir.absolutePath}")
        echo("Package: $packageName")

        var generatedCount = 0
        requestedModules.forEach{ moduleName ->
            val generator = registry.getGenerator(moduleName)
            if (generator == null) {
                echo(
                    "Warning: Unknown module '$moduleName'. Available modules: ${
                        registry.getAllGenerators().keys.joinToString(
                            ", "
                        )
                    }"
                )
                return@forEach
            }

            if (!generator.canGenerate(config)) {
                echo("Skipping '$moduleName' - generation requirements not met")
                return@forEach
            }

            try {
                val generatedFile = generator.generate(config)
                generatedFile.writeTo(config.outputDir, config.packageName)
                echo("✓ Generated ${generatedFile.className}")
                generatedCount++
            } catch (e: Exception) {
                echo("✗ Failed to generate '$moduleName': ${e.message}")
            }
        }

        echo(
            "\nGeneration complete! Generated $generatedCount files in ${
                File(
                    outDir,
                    packageName.replace('.', '/')
                ).absolutePath
            }"
        )
    }
}

fun main(args: Array<String>) {
    GenerateCommand().main(args)
}
