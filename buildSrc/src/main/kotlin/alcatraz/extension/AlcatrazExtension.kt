package alcatraz.extension

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class AlcatrazExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        val packageName: Property<String> = objects.property(String::class.java)

        private val deviceCheck = objects.newInstance(DeviceCheckExtension::class.java)

        fun devicecheck(action: Action<DeviceCheckExtension>) {
            action.execute(deviceCheck)
        }

        fun getDeviceCheck(): DeviceCheckExtension = deviceCheck
    }

open class DeviceCheckExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        val teamIdentifier: Property<String> = objects.property(String::class.java)
    }
