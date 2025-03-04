package alcatraz.server

import com.google.inject.Guice

class Alcatraz {
    class Builder {
        private var environment: Environment = Environment.DEVELOPMENT

        fun withEnvironment(environment: Environment) =
            this.apply {
                this.environment = environment
            }

        fun build(): Alcatraz {
            Guice.createInjector(
                AlcatrazModule(environment),
            )

            return Alcatraz()
        }
    }
}
