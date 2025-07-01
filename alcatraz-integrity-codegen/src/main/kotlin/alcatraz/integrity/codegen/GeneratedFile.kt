package alcatraz.integrity.codegen

import java.io.File

data class GeneratedFile(
    val fileName: String,
    val content: String,
    val className: String = fileName.removeSuffix(".kt")
) {
    fun writeTo(outputDir: File, packageName: String) {
        val packageDir = File(outputDir, packageName.replace('.', '/'))
        packageDir.mkdirs()
        File(packageDir, fileName).writeText(content)
    }
}