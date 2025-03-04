package alcatraz.server

import com.google.inject.AbstractModule

class AlcatrazModule(
    private val environment: Environment,
) : AbstractModule() {
    override fun configure() {
        bind(Environment::class.java).toInstance(environment)
        super.configure()
    }
}
