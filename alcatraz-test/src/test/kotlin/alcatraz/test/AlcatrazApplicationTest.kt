package alcatraz.test

import alcatraz.server.Alcatraz
import org.junit.jupiter.api.Test

class AlcatrazApplicationTest {
    @Test
    fun projectRunTest() {
        Alcatraz
            .Builder()
            .withDeviceCheck(
                Alcatraz.Builder
                    .DeviceCheck(),
            ).build()
    }
}
