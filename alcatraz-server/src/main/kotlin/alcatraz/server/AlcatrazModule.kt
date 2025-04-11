package alcatraz.server

import alcatraz.core.Environment
import alcatraz.core.framework.FrameworkType
import com.google.inject.AbstractModule

class AlcatrazModule(
    private val environment: Environment,
    private val frameworkType: FrameworkType,
    private val feature: Alcatraz.Builder.DeviceCheck,
) : AbstractModule() {
    override fun configure() {
        bind(Alcatraz.Builder.DeviceCheck::class.java).toInstance(feature)
        bind(Environment::class.java).toInstance(environment)
        bind(FrameworkType::class.java).toInstance(frameworkType)
        super.configure()
    }
}
