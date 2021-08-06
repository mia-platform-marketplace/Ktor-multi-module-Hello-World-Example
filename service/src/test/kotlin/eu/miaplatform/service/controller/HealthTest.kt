package eu.miaplatform.service.controller

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.commons.StatusService
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.client.RetrofitClient
import eu.miaplatform.commons.model.HealthBodyResponse
import eu.miaplatform.service.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.Test
import io.ktor.server.testing.withTestApplication
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.event.Level

class HealthTest {
    private val objectMapper = ObjectMapper()

    private val crudClient = RetrofitClient.build<CrudClientInterface>("http://crud-url", HttpLoggingInterceptor.Level.NONE)

    @Test
    fun `Health should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/healthz") {

            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }
    }

    @Test
    fun `Ready should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/ready") {

            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }

    }

    @Test
    fun `Check Up should return OK`() {
        withTestApplication({
            module (
                logLevel = Level.DEBUG,
                crudClient = crudClient,
                headersToProxy = HeadersToProxy()
            )
        }) {
            handleRequest(HttpMethod.Get, "/-/check-up") {

            }.apply {
                assertThat(response.status()?.value == HttpStatusCode.OK.value).isTrue()

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }

    }
}