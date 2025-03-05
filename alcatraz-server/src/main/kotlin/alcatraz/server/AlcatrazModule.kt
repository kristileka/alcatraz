package alcatraz.server

import alcatraz.core.Environment
import alcatraz.core.framework.FrameworkType
import com.google.inject.AbstractModule

class AlcatrazModule(
    private val environment: Environment,
    private val frameworkType: FrameworkType,
) : AbstractModule() {
    override fun configure() {
        bind(Environment::class.java).toInstance(environment)
        bind(FrameworkType::class.java).toInstance(frameworkType)
        super.configure()
    }
}
