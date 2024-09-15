package com.tsmc.mlid

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.*
import org.hamcrest.Matchers.`is` as Is

@QuarkusTest
@QuarkusTestResource(WireMockProxy::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProxyResourceTest {
    private lateinit var wireMockServer: WireMockServer

    @BeforeAll
    fun setup() {
        wireMockServer = WireMockServer(18089)
        wireMockServer.start()
    }

    @AfterAll
    fun teardown() {
        wireMockServer.stop()
    }

    @Test
    fun testGetProxy() {
        wireMockServer.stubFor(
            get(urlEqualTo("/api/users?id=123"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"id": 123, "name": "John Doe"}"""),
                ),
        )

        When {
            get("/proxy/api/users?id=123")
        } Then {
            statusCode(200)
            body("id", Is(123))
            body("name", Is("John Doe"))
        }
    }

    @Test
    fun testPostProxy() {
        wireMockServer.stubFor(
            post(urlEqualTo("/api/users"))
                .withRequestBody(equalToJson("""{"name": "Jane Doe"}"""))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"id": 456, "name": "Jane Doe"}"""),
                ),
        )

        Given {
            body("""{"name": "Jane Doe"}""")
        } When {
            post("/proxy/api/users")
        } Then {
            statusCode(200)
            body("id", Is(456))
            body("name", Is("Jane Doe"))
        }
    }
}

class WireMockProxy : QuarkusTestResourceLifecycleManager {
    override fun start()= mapOf("myapp.proxy.url" to "http://localhost:18089")

    override fun stop() {
    }
}
