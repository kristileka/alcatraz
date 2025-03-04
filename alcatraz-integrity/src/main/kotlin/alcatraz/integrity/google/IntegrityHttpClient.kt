package alcatraz.integrity.google

import alcatraz.integrity.model.TokenPayloadExternal
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse

interface IntegrityHttpClient {
    data class Response(
        val statusCode: Int,
        val headers: HttpHeaders,
        val body: TokenPayloadExternal,
    )

    fun post(
        uri: URI,
        authorizationHeader: Map<String, String>,
        body: String,
    ): Response

    data object SimpleIntegrityHttpClient : IntegrityHttpClient {
        private val httpClient = HttpClient.newHttpClient()
        private val objectMapper = jacksonObjectMapper()

        override fun post(
            uri: URI,
            authorizationHeader: Map<String, String>,
            body: String,
        ): Response {
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(uri)
                    .apply { authorizationHeader.forEach { (k, v) -> header(k, v) } }
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()

            val httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            return Response(
                statusCode = httpResponse.statusCode(),
                headers = httpResponse.headers(),
                body =
                    objectMapper.readValue<TokenPayloadExternal>(
                        httpResponse.body(),
                    ),
            )
        }
    }
}
