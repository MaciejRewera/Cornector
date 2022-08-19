package com.rewera.routing

import com.rewera.models.FlowStatus
import com.rewera.models.RpcFlowOutcomeResponse
import com.rewera.modules.JacksonBuilder
import com.rewera.testdata.TestData.FlowResult
import com.rewera.testdata.TestData.flowHandleWithClientId
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.corda.core.messaging.CordaRPCOps
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FlowStarterRoutesSpec : MockedCordaRpcConnectionIntegrationTestBase() {

    private val rpcOps = Mockito.mock(CordaRPCOps::class.java)

    @BeforeEach
    fun setup() {
        reset(rpcOps)
        whenever(cordaRpcOpsFactory.rpcOps).thenReturn(rpcOps)
    }
    
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
        fun `should return NotFound when no clientid provided`() =
            testApplicationWithMockedRpcConnection {
                whenever(rpcOps.reattachFlowWithClientId<Any>(any())).thenReturn(null)

                val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/")

                response.status shouldBe HttpStatusCode.NotFound
                response.bodyAsText() shouldBe ""
            }

        @Test
        fun `should return NotFound when no flow for clientid found`() =
            testApplicationWithMockedRpcConnection {
                whenever(rpcOps.reattachFlowWithClientId<Any>(any())).thenReturn(null)
                val clientId = "test-client-id"

                val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/$clientId")

                response.status shouldBe HttpStatusCode.NotFound
                response.bodyAsText() shouldBe ""
            }

        @Test
        fun `should return the value from CordaRPCOps`() =
            testApplicationWithMockedRpcConnection {
                val clientId = "test-client-id"
                val testFlowResult = FlowResult("Test value", 1234567)
                whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any()))
                    .thenReturn(flowHandleWithClientId(clientId, testFlowResult))

                val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/$clientId")

                response.status shouldBe HttpStatusCode.OK
                response.bodyAsText() shouldBe
                        """{"exceptionDigest":null,"resultJson":"{\"value1\":\"Test value\",\"value2\":1234567}","status":"COMPLETED"}"""
            }

        @Test
        fun `should return value that is convertable to RpcFlowOutcomeResponse`() =
            testApplicationWithMockedRpcConnection {
                val clientId = "test-client-id"
                val testFlowResult = FlowResult("Test value", 1234567)
                whenever(rpcOps.reattachFlowWithClientId<FlowResult>(any()))
                    .thenReturn(flowHandleWithClientId(clientId, testFlowResult))

                val response = client.get("/api/v1/flowstarter/flowoutcomeforclientid/$clientId")

                JacksonBuilder.jackson.readValue(
                    response.bodyAsText(),
                    RpcFlowOutcomeResponse::class.java
                ) shouldBe RpcFlowOutcomeResponse(
                    status = FlowStatus.COMPLETED,
                    exceptionDigest = null,
                    resultJson = """{"value1":"Test value","value2":1234567}"""
                )
            }
    }

}