package com.tsmc.mlid

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class MyWireMockResource : QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(8090)
        wireMockServer.stubFor(
            get(urlEqualTo("/api/users?id=123"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"id": 123, "name": "John Doe"}"""),
                ),
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/api/users"))
                .withRequestBody(equalToJson("""{"name": "Jane Doe"}"""))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("""{"id": 456, "name": "Jane Doe"}""")
                )
        )
        wireMockServer.start()

        return java.util.Map.of("myapp.proxy.url", "http://localhost:" + wireMockServer.port())
    }

    override fun stop() {
        wireMockServer.stop()
    }
}
