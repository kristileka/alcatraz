package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

class AlcatrazPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create the extension
        val extension = project.extensions.create<AlcatrazExtension>("alcatraz")

        // Find the codegen project
        val codegenProject =
            project.rootProject.findProject(":alcatraz-devicecheck-codegen")
                ?: throw IllegalStateException("Project :alcatraz-devicecheck-codegen not found. Please create this module first.")

        // Define output directory in the project's build directory
        val outputDir =
            project.layout.buildDirectory
                .dir("alcatraz-generated")
                .get()
                .asFile

        // Create the output directory ahead of time
        outputDir.mkdirs()

        // Add generated directory to the project's source sets
        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
        sourceSets?.getByName("main")?.java?.srcDir(outputDir)

        // Add generated directory to dependencies for both compile and runtime
        val javaExtension = project.extensions.getByType<JavaPluginExtension>()
        val targetSources = listOf("main", "test")

        targetSources.forEach {
            javaExtension.sourceSets.named(it) {
                compileClasspath += project.files(outputDir)
                runtimeClasspath += project.files(outputDir)
            }
        }

        project.tasks.register("generateAlcatrazClasses") {
            group = "alcatraz"
            description = "Generates Alcatraz classes based on plugin configuration"

            // Make the task depend on compiling the codegen module
            dependsOn(codegenProject.tasks.named("compileKotlin"))

            doLast {
                // Ensure packageName is specified
                if (!extension.packageName.isPresent) {
                    throw IllegalStateException("packageName must be specified in the alcatraz block")
                }

                val packageName = extension.packageName.get()

                // Build the command-line arguments for the Clikt command
                val args =
                    mutableListOf(
                        "--package",
                        packageName,
                        "--output",
                        outputDir.absolutePath,
                    )

                // Add team identifier if present
                if (extension.getDeviceCheck().teamIdentifier.isPresent) {
                    args.add("--team-id")
                    args.add(extension.getDeviceCheck().teamIdentifier.get())
                }

                // Get the runtime classpath for the codegen module
                val runtimeClasspath =
                    codegenProject.tasks
                        .named("compileKotlin")
                        .get()
                        .outputs.files
                        .plus(codegenProject.configurations.getByName("runtimeClasspath"))

                // Run the Clikt command in the codegen module
                project.javaexec {
                    classpath = runtimeClasspath
                    mainClass.set("alcatraz.devicecheck.codegen.GenerateCommandKt")
                    setArgs(args)
                }

                project.logger.lifecycle("Generated Alcatraz classes using the command-line generator")
            }
        }

        // Make compile task depend on code generation
        project.afterEvaluate {
            project.tasks.findByName("compileKotlin")?.dependsOn("generateAlcatrazClasses")
            project.tasks.findByName("compileJava")?.dependsOn("generateAlcatrazClasses")
        }
    }
}
