package alcatraz.extension

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject

open class AlcatrazExtension @Inject constructor(
    objects: ObjectFactory,
) {
    @get:Input
    val packageName: Property<String> = objects.property(String::class.java)

    @get:Nested
    private val deviceCheck = objects.newInstance(DeviceCheckExtension::class.java)

    @get:Nested
    private val integrity = objects.newInstance(IntegrityExtension::class.java)

    fun devicecheck(action: Action<DeviceCheckExtension>) {
        action.execute(deviceCheck)
    }

    fun integrity(action: Action<IntegrityExtension>) {
        action.execute(integrity)
    }

    fun getDeviceCheck(): DeviceCheckExtension = deviceCheck
    fun getIntegrity(): IntegrityExtension = integrity
}

open class DeviceCheckExtension @Inject constructor(
    objects: ObjectFactory,
) {
    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val teamIdentifier: Property<String> = objects.property(String::class.java)
}

open class IntegrityExtension @Inject constructor(
    objects: ObjectFactory,
) {
    @get:Input
    @get:Optional
    val enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    @get:Input
    @get:Optional
    val token: Property<String> = objects.property(String::class.java)
}
