package com.rewera.routing

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowManagerRoutesIntegrationTest : MockedCordaRpcConnectionIntegrationTestBase() {

    @Nested
    @DisplayName("/flowmanagerrpcops/killflow/{flowid} endpoint")
    inner class KillFlowEndpointSpec {

        @Test
        fun `should return NotFound when no flowid provided`() = testApplicationWithMockedRpcConnection {
            whenever(rpcOps.killFlow(any())).thenReturn(false)

            val response =
                client.post("/api/v1/flowmanagerrpcops/killflow") { contentType(ContentType.Application.Json) }

            response.status shouldBe HttpStatusCode.NotFound
            response.bodyAsText() shouldBe ""
        }

        @Test
        fun `should return Boolean value returned from CordaRPCOps`() = testApplicationWithMockedRpcConnection {
            whenever(rpcOps.killFlow(any())).thenReturn(true)
            val flowId = UUID.randomUUID().toString()

            val response =
                client.post("/api/v1/flowmanagerrpcops/killflow/$flowId") { contentType(ContentType.Application.Json) }

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldBe "true"
        }
    }
}