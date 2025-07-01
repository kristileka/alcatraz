package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension
import java.io.File

class ArgumentBuilder {

    fun buildArgs(
        extension: AlcatrazExtension,
        moduleName: String,
        packageName: String,
        outputDir: File
    ): List<String> {
        val args = mutableListOf(
            "--package", packageName,
            "--output", outputDir.absolutePath
        )

        when (moduleName) {
            "devicecheck" -> addDeviceCheckArgs(args, extension)
            "integrity" -> addIntegrityArgs(args, extension)
        }

        return args
    }

    private fun addDeviceCheckArgs(args: MutableList<String>, extension: AlcatrazExtension) {
        val deviceCheck = extension.getDeviceCheck()

        deviceCheck.teamIdentifier.orNull?.let {
            args.add("--team-id")
            args.add(it)
        }
    }

    private fun addIntegrityArgs(args: MutableList<String>, extension: AlcatrazExtension) {
        val integrity = extension.getIntegrity()

        integrity.token.orNull?.let {
            args.add("--token")
            args.add(it)
        }
    }
}

