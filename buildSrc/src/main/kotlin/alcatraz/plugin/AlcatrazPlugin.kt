package alcatraz.plugin

import alcatraz.extension.AlcatrazExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class AlcatrazPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create<AlcatrazExtension>("alcatraz")

        val projectSetup = ProjectSetup(project)
        val taskManager = TaskManager(project, extension)

        projectSetup.configureSourceSets()
        taskManager.createTasks()
        projectSetup.configureDependencies()
    }
}
