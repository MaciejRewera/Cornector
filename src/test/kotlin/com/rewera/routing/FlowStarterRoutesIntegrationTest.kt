package com.rewera.routing

import com.rewera.models.api.*
import com.rewera.modules.Jackson
import com.rewera.testdata.TestData
import com.rewera.testdata.TestData.TestFlowResult
import com.rewera.testdata.TestData.flowHandleWithClientId
import com.rewera.testdata.TestData.testClientId
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowStarterRoutesIntegrationTest : MockedCordaRpcConnectionIntegrationTestBase() {

    @Test
    fun `registeredflows endpoint should return the value from CordaRPCOps`() = testApplicationWithMockedRpcConnection {
        whenever(rpcOps.registeredFlows()).thenReturn(listOf("com.test.FlowName.1", "com.test.FlowName.2"))

        val response = client.get("/api/v1/flowstarter/registeredflows")

        response.status shouldBe HttpStatusCode.OK
        response.bodyAsText() shouldBe """["com.test.FlowName.1","com.test.FlowName.2"]"""
    }

    @Nested
    @DisplayName("/flowoutcomeforclientid/{clientid} endpoint")
    inner class FlowOutcomeForClientIdEndpointSpec {

        @Test
        fun `should return NotFound when no clientid provided`() = testApplicationWithMockedRpcConnection {
            whenever(rpcOps.reattachFlowWithClientId<Any>(any())).thenReturn(null)

            val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/")

            response.status shouldBe HttpStatusCode.NotFound
            response.bodyAsText() shouldBe ""
        }

        @Test
        fun `should return NotFound when NO flow for clientid found`() = testApplicationWithMockedRpcConnection {
            whenever(rpcOps.reattachFlowWithClientId<Any>(any())).thenReturn(null)

            val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/$testClientId")

            response.status shouldBe HttpStatusCode.NotFound
            response.bodyAsText() shouldBe ""
        }

        @Test
        fun `should return the value from CordaRPCOps when flow for clientid has been found`() =
            testApplicationWithMockedRpcConnection {
                val testFlowResult = TestFlowResult("Test value", 1234567)
                whenever(rpcOps.reattachFlowWithClientId<TestFlowResult>(any()))
                    .thenReturn(flowHandleWithClientId(testClientId, testFlowResult))

                val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/$testClientId")

                response.status shouldBe HttpStatusCode.OK
                response.bodyAsText() shouldBe
                        """{"exceptionDigest":null,"resultJson":"{\"value1\":\"Test value\",\"value2\":1234567}","status":"COMPLETED"}"""

                Jackson.mapper.readValue(
                    response.bodyAsText(),
                    RpcFlowOutcomeResponse::class.java
                ) shouldBe RpcFlowOutcomeResponse(
                    status = FlowStatus.COMPLETED,
                    exceptionDigest = null,
                    resultJson = """{"value1":"Test value","value2":1234567}"""
                )
            }
    }

    @Nested
    @DisplayName("/startflow endpoint")
    inner class StartFlowEndpointSpec {

        @Test
        fun `should return RpcStartFlowResponse in JSON format`() = testApplicationWithMockedRpcConnection {
            val testFlowResult = TestFlowResult("Test value", 1234567)
            val flowHandle = flowHandleWithClientId(testClientId, testFlowResult)
            whenever(rpcOps.startFlowDynamicWithClientId<TestFlowResult>(any(), any(), any())).thenReturn(flowHandle)

            val client = createClient { install(ContentNegotiation) { jackson() } }
            val requestBody = RpcStartFlowRequest(
                testClientId,
                TestData.SingleParameterTestFlow::class.java.name,
                RpcStartFlowRequestParameters("""{"someParameter":"Test value"}""")
            )

            val response = client.post("/api/v1/flowstarter/startflow") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldBe """{"clientId":"$testClientId","flowId":{"uuid":"${flowHandle.id.uuid}"}}"""

            Jackson.mapper.readValue(
                response.bodyAsText(),
                RpcStartFlowResponse::class.java
            ) shouldBe RpcStartFlowResponse(clientId = testClientId, flowId = FlowId(flowHandle.id.uuid))
        }
    }
}