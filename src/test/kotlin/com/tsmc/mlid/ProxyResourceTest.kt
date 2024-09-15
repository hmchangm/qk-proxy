package com.tsmc.mlid

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.`is` as Is

@QuarkusTest
@QuarkusTestResource(MyWireMockResource::class)
class ProxyResourceTest {
    @Test
    fun testGetProxy() {
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
