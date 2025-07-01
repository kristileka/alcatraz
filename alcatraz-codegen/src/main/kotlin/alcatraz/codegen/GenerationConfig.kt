package alcatraz.codegen

import java.io.File

data class GenerationConfig(
    val packageName: String,
    val outputDir: File,
    val teamIdentifier: String? = null,
    val basePath: String = "alcatraz",
    val additionalParams: Map<String, Any> = emptyMap()
)