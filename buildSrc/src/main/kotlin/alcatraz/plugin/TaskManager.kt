package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class TaskManager(
    private val project: Project,
    private val extension: AlcatrazExtension
) {
    private val moduleRegistry = ModuleRegistry()
    private val projectSetup = ProjectSetup(project)
    
    fun createTasks() {
        createIndividualModuleTasks()
        createMasterTask()
    }
    
    private fun createIndividualModuleTasks() {
        moduleRegistry.getAllModules().forEach { (moduleName, moduleConfig) ->
            val taskCreator = TaskCreator(project, extension, projectSetup.outputDir)
            taskCreator.createTask(moduleName, moduleConfig)
        }
    }
    
    private fun createMasterTask() {
        project.tasks.register("generateAlcatrazClasses") {
            group = "alcatraz"
            description = "Generates all enabled Alcatraz classes based on plugin configuration"
            
            doFirst {
                project.logger.lifecycle("Starting Alcatraz code generation...")
            }
            
            doLast {
                project.logger.lifecycle("Completed Alcatraz code generation for all enabled modules")
            }
        }
        
        configureMasterTaskDependencies()
    }
    
    private fun configureMasterTaskDependencies() {
        project.afterEvaluate {
            val generateTask = project.tasks.named("generateAlcatrazClasses")
            val enabledModules = moduleRegistry.getEnabledModules(extension)
            
            enabledModules.forEach { moduleName ->
                val moduleTask = project.tasks.findByName("generate${moduleName.capitalized()}Classes")
                moduleTask?.let {
                    generateTask.configure { dependsOn(it) }
                }
            }
        }
    }
}
