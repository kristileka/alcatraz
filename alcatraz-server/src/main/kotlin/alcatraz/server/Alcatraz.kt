package alcatraz.server

import alcatraz.core.AlcatrazCoreBuilder
import alcatraz.core.Environment
import alcatraz.core.framework.AlcatrazFrameworkDetector
import com.google.inject.Guice

class Alcatraz {
    class Builder {
        private var environment: Environment = Environment.DEVELOPMENT

        fun withEnvironment(environment: Environment) =
            this.apply {
                this.environment = environment
            }

        fun build() {
            val frameworkAdapter = AlcatrazFrameworkDetector.detectFramework()
            val injector =
                Guice.createInjector(
                    AlcatrazModule(
                        environment,
                        frameworkAdapter,
                    ),
                )

            injector.getInstance(AlcatrazCoreBuilder::class.java).generate()
        }
    }
}
