package alcatriaz.test

import alcatriaz.server.Alcatriaz
import org.junit.jupiter.api.Test;

class AlcatriazApplicationTest {

    @Test
    fun projectRunTest() {
        val alcatriaz = Alcatriaz.Builder().build()
    }
}