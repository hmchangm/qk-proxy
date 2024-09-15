package com.tsmc.mlid

import io.quarkus.logging.Log
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.HttpResponse
import io.vertx.mutiny.ext.web.client.WebClient
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/proxy")
class ProxyResource(
    private val vertx: Vertx,
) {
    val webClient: WebClient =
        WebClient.create(
            vertx,
            WebClientOptions().setConnectTimeout(30000).setIdleTimeout(30000).setTrustAll(true),
        )

    @ConfigProperty(name = "myapp.proxy.url")
    lateinit var baseUrl: String

    @POST
    @Path("{path:.*}")
    suspend fun forwardPost(
        @PathParam("path") path: String,
        body: String,
        @Context uriInfo: UriInfo,
    ): Response {
        val url = "$baseUrl/$path".also { Log.info("post url $it") }
        return webClient
            .postAbs(url)
            .putHeader("Authorization", "Basic FSDFSDFSD")
            .sendBuffer(Buffer.buffer(body))
            .awaitSuspending()
            .let { response -> createResponse(response) }
    }

    @GET
    @Path("{path:.*}")
    suspend fun forwardGet(
        @PathParam("path") path: String,
        @Context uriInfo: UriInfo,
    ): Response {
        val url = "$baseUrl/$path".also { Log.info("get url $it") }
        val request =
            webClient
                .getAbs(url)
                .putHeader("Authorization", "Basic FSDFSDFSD")

        // Add query parameters to the request
        uriInfo.queryParameters.forEach { (name, values) ->
            values.forEach { value ->
                request.addQueryParam(name, value)
            }
        }

        return request
            .send()
            .awaitSuspending()
            .let { response -> createResponse(response) }
    }

    private fun createResponse(response: HttpResponse<Buffer>): Response {
        val responseBuilder = Response.status(response.statusCode())

        response.headers().forEach { header ->
            responseBuilder.header(header.key, header.value)
        }

        return responseBuilder.entity(response.bodyAsString()).build()
    }
}
