package alcatraz.core

import alcatraz.core.framework.FrameworkType
import com.google.inject.Inject

class AlcatrazCoreBuilder
    @Inject
    constructor(
        val frameworkType: FrameworkType,
        val environment: Environment,
    ) {
        fun generate() {
            print(frameworkType)
        }
    }
