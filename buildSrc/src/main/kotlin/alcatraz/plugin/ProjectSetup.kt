package alcatraz.plugin

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.File

class ProjectSetup(private val project: Project) {

    val outputDir: File = project.layout.buildDirectory
        .dir("alcatraz-generated")
        .get()
        .asFile

    fun configureSourceSets() {
        outputDir.mkdirs()

        val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
        sourceSets?.getByName("main")?.java?.srcDir(outputDir)

        val javaExtension = project.extensions.getByType<JavaPluginExtension>()
        listOf("main", "test").forEach { sourceSet ->
            javaExtension.sourceSets.named(sourceSet) {
                compileClasspath += project.files(outputDir)
                runtimeClasspath += project.files(outputDir)
            }
        }
    }

    fun configureDependencies() {
        project.afterEvaluate {
            project.tasks.findByName("compileKotlin")?.dependsOn("generateAlcatrazClasses")
            project.tasks.findByName("compileJava")?.dependsOn("generateAlcatrazClasses")
        }
    }
}
