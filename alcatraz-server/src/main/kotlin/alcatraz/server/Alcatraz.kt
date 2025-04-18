package alcatraz.server

import alcatraz.core.AlcatrazCoreBuilder
import alcatraz.core.Environment
import alcatraz.core.framework.AlcatrazFrameworkDetector
import alcatraz.devicecheck.receipt.AppleJwsGenerator
import com.google.inject.Guice

class Alcatraz {
    class Builder {
        private var environment: Environment = Environment.DEVELOPMENT
        private var deviceCheck: DeviceCheck? = null

        fun withEnvironment(environment: Environment) =
            this.apply {
                this.environment = environment
            }

        fun withDeviceCheck(deviceCheck: DeviceCheck) =
            apply {
                this.deviceCheck = deviceCheck
            }

        fun build() {
            val frameworkAdapter = AlcatrazFrameworkDetector.detectFramework()
            val injector =
                Guice.createInjector(
                    AlcatrazModule(
                        environment,
                        frameworkAdapter,
                        deviceCheck!!,
                    ),
                )

            injector.getInstance(AlcatrazCoreBuilder::class.java).generate()
        }

        class DeviceCheck : Feature {
            lateinit var teamIdentifier: String
            lateinit var bundleIdentifier: String
            lateinit var appleJwsGenerator: AppleJwsGenerator

            fun withBundleIdentifier(bundleIdentifier: String) =
                apply {
                    this.bundleIdentifier = bundleIdentifier
                }

            fun withAppleJwsGenerator(appleJwsGenerator: AppleJwsGenerator) =
                apply {
                    this.appleJwsGenerator = appleJwsGenerator
                }

            fun withTeamIdentifier(teamIdentifier: String) =
                apply {
                    this.teamIdentifier = teamIdentifier
                }
        }
    }
}
