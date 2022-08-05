package com.rewera

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*

class ApplicationTest : WordSpec({

    "testRoot" should {
        "pass for root endpoint" {
            testApplication {
                val response = client.get("/")

                HttpStatusCode.OK shouldBe response.status
                response.bodyAsText() shouldBe "Hello World!"
            }
        }
    }
})