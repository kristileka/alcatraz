package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension
import org.gradle.api.Project
import java.io.File

class TaskCreator(
    private val project: Project,
    private val extension: AlcatrazExtension,
    private val outputDir: File
) {
    private val moduleRegistry = ModuleRegistry()
    private val argumentBuilder = ArgumentBuilder()

    fun createTask(moduleName: String, moduleConfig: CodegenModule) {
        val taskName = "generate${moduleName.capitalize()}Classes"

        project.tasks.register(taskName) {
            group = "alcatraz"
            description = "Generates ${moduleConfig.description}"

            val codegenProject = findCodegenProject(moduleConfig.projectName)
            if (codegenProject == null) {
                project.logger.warn("Codegen project ${moduleConfig.projectName} not found. Skipping $moduleName generation.")
                enabled = false
                return@register
            }

            dependsOn(codegenProject.tasks.named("compileKotlin"))

            doLast {
                executeCodeGeneration(moduleName, moduleConfig, codegenProject)
            }
        }
    }

    private fun findCodegenProject(projectName: String) =
        project.rootProject.findProject(projectName)

    private fun executeCodeGeneration(
        moduleName: String,
        moduleConfig: CodegenModule,
        codegenProject: Project
    ) {
        if (!moduleRegistry.isModuleEnabled(extension, moduleName)) {
            project.logger.lifecycle("Module $moduleName is disabled, skipping generation")
            return
        }

        validateConfiguration()

        val packageName = extension.packageName.get()
        val args = argumentBuilder.buildArgs(extension, moduleName, packageName, outputDir)
        val runtimeClasspath = buildRuntimeClasspath(codegenProject)

        project.javaexec {
            classpath = runtimeClasspath
            mainClass.set(moduleConfig.mainClass)
            setArgs(args)
        }

        project.logger.lifecycle("Generated $moduleName classes using ${moduleConfig.mainClass}")
    }

    private fun validateConfiguration() {
        if (!extension.packageName.isPresent) {
            throw IllegalStateException("packageName must be specified in the alcatraz block")
        }
    }

    private fun buildRuntimeClasspath(codegenProject: org.gradle.api.Project) =
        codegenProject.tasks
            .named("compileKotlin")
            .get()
            .outputs.files
            .plus(codegenProject.configurations.getByName("runtimeClasspath"))
}
