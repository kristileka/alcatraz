package alcatraz.spring.test

import alcatraz.integrity.model.RegisterDevice
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
class IntegrityControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `test customer endpoint exists and returns expected content`() {
        // Arrange
        val requestBody = "{\"deviceId\":\"" + UUID.randomUUID() + "\"}"

        // Act - First call to capture the response
        val firstResponse = mockMvc
            .perform(
                post("/alcatraz/challenge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsString

        // Act - Second call with same request
        val secondResponse = mockMvc
            .perform(
                post("/alcatraz/challenge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .response
            .contentAsString

        assertEquals(firstResponse, secondResponse)
    }
}
