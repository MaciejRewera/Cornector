package com.rewera.controllers

import com.rewera.connectors.CordaNodeConnector
import com.rewera.connectors.FlowExecutor
import com.rewera.models.FlowResult
import com.rewera.models.api.*
import com.rewera.repositories.FlowResultRepository
import com.rewera.testdata.TestData.TestFlowResult
import com.rewera.testdata.TestData.randomUuid
import com.rewera.testdata.TestData.randomUuidString
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.server.plugins.*
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
    private val flowExecutor = mock(FlowExecutor::class.java)
    private val flowResultRepository = mock(FlowResultRepository::class.java)

    private val flowStarterController = FlowStarterController(cordaNodeConnector, flowExecutor, flowResultRepository)

    @BeforeEach
    fun setup() {
        reset(cordaNodeConnector, flowExecutor, flowResultRepository)
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

        @Test
        fun `should call FlowResultRepository`() {
            whenever(flowResultRepository.findByClientId(any())).thenReturn(FlowResult<Any>(testClientId))

            flowStarterController.getFlowOutcomeForClientId(testClientId)

            verify(flowResultRepository).findByClientId(eq(testClientId))
        }

        @Test
        fun `should throw NotFoundException when FlowResultRepository returns null`() {
            whenever(flowResultRepository.findByClientId(any())).thenReturn(null)

            val exc = shouldThrow<NotFoundException> { flowStarterController.getFlowOutcomeForClientId(testClientId) }
            exc.message shouldBe "Resource not found"
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status RUNNING when FlowResultRepository returns FlowResult with status RUNNING`() {
            whenever(flowResultRepository.findByClientId(any())).thenReturn(
                FlowResult<Any>(testClientId, flowId = randomUuidString(), status = FlowStatus.RUNNING)
            )

            val result = flowStarterController.getFlowOutcomeForClientId(testClientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.RUNNING,
                exceptionDigest = null,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status FAILED when FlowResultRepository returns FlowResult with status FAILED`() {
            val exceptionDigest = ExceptionDigest(
                exceptionType = CompletionException::class.java.name,
                message = "Something went wrong in the flow"
            )
            whenever(flowResultRepository.findByClientId(any())).thenReturn(
                FlowResult<Any>(
                    testClientId,
                    flowId = randomUuidString(),
                    status = FlowStatus.FAILED,
                    exceptionDigest = exceptionDigest
                )
            )

            val result = flowStarterController.getFlowOutcomeForClientId(testClientId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.FAILED,
                exceptionDigest = exceptionDigest,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status COMPLETED when FlowResultRepository returns FlowResult with status COMPLETED`() {
            whenever(flowResultRepository.findByClientId(any())).thenReturn(
                FlowResult(
                    testClientId,
                    flowId = randomUuidString(),
                    result = TestFlowResult("Test value", 1234567),
                    status = FlowStatus.COMPLETED
                )
            )

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
    @DisplayName("FlowStarterController on getFlowOutcomeForFlowId")
    inner class GetFlowOutcomeForFlowIdSpec {

        private val flowId = randomUuid()

        @Test
        fun `should call FlowResultRepository`() {
            whenever(flowResultRepository.findByFlowId(any())).thenReturn(FlowResult<Any>(testClientId))

            flowStarterController.getFlowOutcomeForFlowId(flowId)

            verify(flowResultRepository).findByFlowId(eq(flowId))
        }

        @Test
        fun `should throw NotFoundException when FlowResultRepository returns null`() {
            whenever(flowResultRepository.findByFlowId(any())).thenReturn(null)

            val exc = shouldThrow<NotFoundException> { flowStarterController.getFlowOutcomeForFlowId(flowId) }
            exc.message shouldBe "Resource not found"
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status RUNNING when FlowResultRepository returns FlowResult with status RUNNING`() {
            whenever(flowResultRepository.findByFlowId(any())).thenReturn(
                FlowResult<Any>(testClientId, flowId = randomUuidString(), status = FlowStatus.RUNNING)
            )

            val result = flowStarterController.getFlowOutcomeForFlowId(flowId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.RUNNING,
                exceptionDigest = null,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status FAILED when FlowResultRepository returns FlowResult with status FAILED`() {
            val exceptionDigest = ExceptionDigest(
                exceptionType = CompletionException::class.java.name,
                message = "Something went wrong in the flow"
            )
            whenever(flowResultRepository.findByFlowId(any())).thenReturn(
                FlowResult<Any>(
                    testClientId,
                    flowId = randomUuidString(),
                    status = FlowStatus.FAILED,
                    exceptionDigest = exceptionDigest
                )
            )

            val result = flowStarterController.getFlowOutcomeForFlowId(flowId)

            val expectedResult = RpcFlowOutcomeResponse(
                status = FlowStatus.FAILED,
                exceptionDigest = exceptionDigest,
                resultJson = null
            )

            result shouldBe expectedResult
        }

        @Test
        fun `should return RpcFlowOutcomeResponse with status COMPLETED when FlowResultRepository returns FlowResult with status COMPLETED`() {
            whenever(flowResultRepository.findByFlowId(any())).thenReturn(
                FlowResult(
                    testClientId,
                    flowId = randomUuidString(),
                    result = TestFlowResult("Test value", 1234567),
                    status = FlowStatus.COMPLETED
                )
            )

            val result = flowStarterController.getFlowOutcomeForFlowId(flowId)

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
        private val jsonParams = RpcStartFlowRequestParameters("There should be parameters in JSON")

        @Test
        fun `should call FlowExecutor`() {
            whenever(flowExecutor.startFlow(any(), any(), any()))
                .thenReturn(RpcStartFlowResponse("This is test startFlow response", FlowId(UUID.randomUUID())))
            val rpcStartFlowRequest = RpcStartFlowRequest(testClientId, flowName, jsonParams)

            flowStarterController.startFlow(rpcStartFlowRequest)

            verify(flowExecutor).startFlow(eq(testClientId), eq(flowName), eq(jsonParams))
        }

        @Test
        fun `should return value returned by FlowExecutor`() {
            val response = RpcStartFlowResponse("This is test startFlow response", FlowId(UUID.randomUUID()))
            whenever(flowExecutor.startFlow(any(), any(), any())).thenReturn(response)
            val rpcStartFlowRequest = RpcStartFlowRequest(testClientId, flowName, jsonParams)

            flowStarterController.startFlow(rpcStartFlowRequest) shouldBe response
        }
    }

}
