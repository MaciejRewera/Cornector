package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import com.rewera.models.api.*
import com.rewera.testdata.TestData.TestFlowResult
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.*
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.internal.concurrent.openFuture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import java.util.*
import java.util.concurrent.CompletionException


class FlowStarterControllerSpec {

    private val cordaNodeConnector = mock(CordaNodeConnector::class.java)

    private val flowStarterController = FlowStarterController(cordaNodeConnector)

    @BeforeEach
    fun setup() {
        reset(cordaNodeConnector)
    }

    @Nested
    @DisplayName("FlowStarterController on getRegisteredFlows")
    inner class GetRegisteredFlowsSpec {

        @Test
        fun `should call CordaNodeConnector`() {
            whenever(cordaNodeConnector.getRegisteredFlows()).thenReturn(emptyList())

            flowStarterController.getRegisteredFlows()

            verify(cordaNodeConnector).getRegisteredFlows()
        }

        @Test
        fun `should return value returned by CordaNodeConnector`() {
            val flows = listOf("test.flow.1", "test.flow.2", "test.flow.3")
            whenever(cordaNodeConnector.getRegisteredFlows()).thenReturn(flows)

            flowStarterController.getRegisteredFlows() shouldBe flows
        }
    }

    @Nested
    @DisplayName("FlowStarterController on getFlowOutcomeForClientId")
    inner class GetFlowOutcomeForClientIdSpec {

        private val testReturnValue = TestFlowResult("Test value", 1234567)

        @Test
        fun `should call CordaNodeConnector`() {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(any()))
                .thenReturn(doneFuture(testReturnValue).toCompletableFuture())

            flowStarterController.getFlowOutcomeForClientId(testClientId)

            verify(cordaNodeConnector).getFlowOutcomeForClientId<TestFlowResult>(eq(testClientId))
        }

        @Test
        fun `should throw NotFoundException when CordaNodeConnector returns null`() {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(any())).thenReturn(null)

            shouldThrow<NotFoundException> { flowStarterController.getFlowOutcomeForClientId(testClientId) }
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status RUNNING when CordaNodeConnector returns unfinished future`() {
            val unfinishedFuture = openFuture<TestFlowResult>().toCompletableFuture()
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(any())).thenReturn(unfinishedFuture)

            val result = flowStarterController.getFlowOutcomeForClientId(testClientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.RUNNING,
                exceptionDigest = null,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status FAILED when CordaNodeConnector returns exceptionally finished future`() {
            val failedFuture = openFuture<TestFlowResult>().toCompletableFuture()
            val exceptionMessage = "Something went wrong in the flow"
            failedFuture.completeExceptionally(RuntimeException(exceptionMessage))
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(any())).thenReturn(failedFuture)

            val result = flowStarterController.getFlowOutcomeForClientId(testClientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.FAILED,
                exceptionDigest = ExceptionDigest(
                    CompletionException::class.java.name,
                    "${RuntimeException::class.java.name}: $exceptionMessage"
                ),
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status COMPLETED when CordaNodeConnector returns finished future`() {
            whenever(cordaNodeConnector.getFlowOutcomeForClientId<TestFlowResult>(any()))
                .thenReturn(doneFuture(testReturnValue).toCompletableFuture())

            val result = flowStarterController.getFlowOutcomeForClientId(testClientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.COMPLETED,
                exceptionDigest = null,
                resultJson = """{"value1":"Test value","value2":1234567}"""
            )

            result shouldBe expectedResult
        }
    }

    @Nested
    @DisplayName("FlowStarterController on startFlow")
    inner class StartFlowSpec {

        private val flowName = "test.flow.name"
        private val jsonParams =  RpcStartFlowRequestParameters("There should be parameters in JSON")

        @Test
        fun `should call CordaNodeConnector`() {
            whenever(cordaNodeConnector.startFlow(any(), any(), any()))
                .thenReturn(RpcStartFlowResponse("This is test startFlow response", FlowId(UUID.randomUUID())))
            val rpcStartFlowRequest = RpcStartFlowRequest(testClientId, flowName, jsonParams)

            flowStarterController.startFlow(rpcStartFlowRequest)

            verify(cordaNodeConnector).startFlow(eq(testClientId), eq(flowName), eq(jsonParams))
        }

        @Test
        fun `should return value returned by CordaNodeConnector`() {
            val response = RpcStartFlowResponse("This is test startFlow response", FlowId(UUID.randomUUID()))
            whenever(cordaNodeConnector.startFlow(any(), any(), any())).thenReturn(response)
            val rpcStartFlowRequest = RpcStartFlowRequest(testClientId, flowName, jsonParams)

            flowStarterController.startFlow(rpcStartFlowRequest) shouldBe response
        }
    }

}
