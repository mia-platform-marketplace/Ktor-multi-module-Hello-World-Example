package eu.miaplatform.service.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.client.RetrofitClient
import eu.miaplatform.service.model.ErrorResponse
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import eu.miaplatform.service.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.junit.jupiter.api.TestInstance
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.event.Level

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelloWorldTest {

    private val objectMapper = ObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
    private val host = "localhost"
    private val port = 3000
    private var mockServer: MockServerClient = MockServerClient(host, port)

    private val crudClient = RetrofitClient.build<CrudClientInterface>("http://$host:$port", HttpLoggingInterceptor.Level.NONE)

    @Test
    fun `Get should return success object message`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/hello") {
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    HelloWorldResponse(
                        null,
                        null,
                        "Hello world!"
                    )
                )
                assertThat(expectedBody, response.content)
            }
        }
    }

    @Test
    fun `Get should return success object message with query parameter`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/hello?queryParam=param") {
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    HelloWorldResponse(
                        null,
                        "param",
                        "Hello world!"
                    )
                )
                assertThat(expectedBody, response.content)
            }
        }
    }

    @Test
    fun `Post should return success object message with path param`() {

        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            val body = objectMapper.writeValueAsString(
                HelloWorldRequestBody("name", "surname")
            )

            handleRequest(HttpMethod.Post, "/hello/1234") {
                addHeader("Content-Type", "application/json")
                setBody(body)
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    HelloWorldResponse(
                        "1234",
                        null,
                        "Hello world name surname!"
                    )
                )
                assertThat(expectedBody, response.content)
            }
        }
    }

    @Test
    fun `Post should return bad request if body is malformed`() {

        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            val body = objectMapper.writeValueAsString(
                mapOf("name" to "name")
            )

            handleRequest(HttpMethod.Post, "/hello/1234") {
                addHeader("Content-Type", "application/json")
                setBody(body)
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.BadRequest.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    // if you change the package name, remember to update it in the following error or the test will fail
                    ErrorResponse(1000, "Instantiation of [simple type, class eu.miaplatform.service.model.request.HelloWorldRequestBody] value failed for JSON property surname due to missing (therefore NULL) value for creator parameter surname which is a non-nullable type\n at [Source: (InputStreamReader); line: 1, column: 15] (through reference chain: eu.miaplatform.service.model.request.HelloWorldRequestBody[\"surname\"])")
                )
                assertThat(expectedBody).isEqualTo(response.content)
            }
        }
    }

    @Test
    fun `Get with call should return success object message`() {
        mockServer = ClientAndServer.startClientAndServer(port)
        mockServer.setup(
            "GET",
            "/v2/books",
            200,
            objectMapper.writeValueAsString(listOf("book1", "book2"))
        )

        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/hello/with-call") {
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    HelloWorldResponse(
                        null,
                        null,
                        "Hello world! Book list: book1, book2"
                    )
                )
                assertThat(expectedBody).isEqualTo(response.content)
            }
        }
    }

    @Test
    fun `Get with call should return error if call fails`() {
        mockServer = ClientAndServer.startClientAndServer(port)
        mockServer.setup(
            "GET",
            "/v2/books",
            500,
            "{\"error\": \"error\"}"
        )

        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/hello/with-call") {
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.InternalServerError.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    ErrorResponse(1002, "books call failed")
                )
                assertThat(expectedBody).isEqualTo(response.content)
            }
        }
        mockServer.close()
    }

    @Test
    fun `Get with call should return success object message with query parameter`() {
        mockServer = ClientAndServer.startClientAndServer(port)
        mockServer.setup(
            "GET",
            "/v2/books",
            200,
            objectMapper.writeValueAsString(listOf("book1", "book2"))
        )
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/hello/with-call?queryParam=param") {
            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val expectedBody = objectMapper.writeValueAsString(
                    HelloWorldResponse(
                        null,
                        "param",
                        "Hello world! Book list: book1, book2"
                    )
                )
                assertThat(expectedBody).isEqualTo(response.content)
            }
        }
        mockServer.close()
    }

    private fun MockServerClient.setup(
        requestMethod:String,
        requestPath:String,
        responseStatus: Int,
        responseBody:String
    ) {

        this.`when`(
            HttpRequest.request()
                .withMethod(requestMethod)
                .withPath(requestPath)

        )
            .respond(
                HttpResponse.response()
                    .withStatusCode(responseStatus)
                    .withBody(responseBody)
            )
    }

}