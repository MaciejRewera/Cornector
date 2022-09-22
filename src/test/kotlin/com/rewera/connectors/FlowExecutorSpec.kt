package com.rewera.connectors

import com.rewera.models.api.FlowStatus
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.repositories.FlowResultRepository
import com.rewera.testdata.TestData
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandleWithClientIdImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*

class FlowExecutorSpec {

    private val cordaRpcOps = Mockito.mock(CordaRPCOps::class.java)
    private val flowResultRepository = Mockito.mock(FlowResultRepository::class.java)
    private val flowClassBuilder = Mockito.mock(FlowClassBuilder::class.java)

    private val flowExecutor = FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

    @BeforeEach
    fun setup() {
        reset(cordaRpcOps, flowResultRepository, flowClassBuilder)
    }

    @Nested
    @DisplayName("FlowExecutor on startFlow")
    inner class StartFlowSpec {

        private val flowName = TestData.SingleParameterTestFlow::class.java.name
        private val flowIdValue = UUID.randomUUID()
        private val flowResult = "Result"
        private val flowHandle =
            FlowHandleWithClientIdImpl(StateMachineRunId(flowIdValue), doneFuture(flowResult), testClientId)
        private val flowParams = RpcStartFlowRequestParameters("This should be a JSON")

        private val testException = RuntimeException("Test Exception")

        @BeforeEach
        fun subSetup() {
            whenever(flowClassBuilder.buildFlowClass(any())).thenReturn(TestData.SingleParameterTestFlow::class.java)
            whenever(cordaRpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenReturn(flowHandle)
        }

        @Test
        fun `should call FlowClassBuilder`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(flowClassBuilder).buildFlowClass(eq(flowName))
        }

        @Test
        fun `should call FlowResultsRepository insertWithClientId`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(flowResultRepository).insertWithClientId(eq(testClientId))
        }

        @Test
        fun `should call CordaRPCOps startFlowDynamicWithClientId`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(cordaRpcOps).startFlowDynamicWithClientId(
                eq(testClientId),
                eq(TestData.SingleParameterTestFlow::class.java),
                eq(flowParams.parametersInJson)
            )
        }

        @Test
        fun `should call FlowResultsRepository updateFlowId`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(flowResultRepository).updateFlowId(eq(testClientId), eq(flowIdValue))
        }

        @Test
        fun `should call FlowResultsRepository update`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(flowResultRepository).update(
                eq(testClientId),
                eq(flowIdValue),
                eq(FlowStatus.COMPLETED),
                eq(flowResult)
            )
        }

        @Test
        fun `should call CordaRPCOps removeClientId`() {
            flowExecutor.startFlow(testClientId, flowName, flowParams)

            verify(cordaRpcOps).removeClientId(eq(testClientId))
        }

        @Test
        fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
            val result = flowExecutor.startFlow(testClientId, flowName, flowParams)

            result.clientId shouldBe testClientId
            result.flowId.uuid shouldBe flowIdValue
        }

        @Nested
        @DisplayName("when FlowClassBuilder throws an exception")
        inner class StartFlowFlowClassBuilderThrowsExceptionSpec {

            @Test
            fun `should NOT call FlowResultsRepository at all`() {
                whenever(flowClassBuilder.buildFlowClass(any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verifyNoInteractions(flowResultRepository)
            }

            @Test
            fun `should NOT call CordaRPCOps at all`() {
                whenever(flowClassBuilder.buildFlowClass(any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verifyNoInteractions(cordaRpcOps)
            }

            @Test
            fun `should throw this exception`() {
                whenever(flowClassBuilder.buildFlowClass(any())).thenThrow(testException)

                val exc = shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }
                exc shouldBe testException
            }
        }

        @Nested
        @DisplayName("when FlowResultsRepository on insertWithClientId throws an exception")
        inner class StartFlowRepositoryInsertWithClientIdThrowsExceptionSpec {

            @Test
            fun `should NOT call FlowResultsRepository anymore`() {
                whenever(flowResultRepository.insertWithClientId(any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verify(flowResultRepository, never()).updateFlowId(any(), any())
                verify(flowResultRepository, never()).update<String>(any(), any(), any(), any())
            }

            @Test
            fun `should NOT call CordaRPCOps at all`() {
                whenever(flowResultRepository.insertWithClientId(any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verifyNoInteractions(cordaRpcOps)
            }

            @Test
            fun `should throw this exception`() {
                whenever(flowResultRepository.insertWithClientId(any())).thenThrow(testException)

                val exc = shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }
                exc shouldBe testException
            }
        }

        @Nested
        @DisplayName("when CordaRPCOps on startFlowDynamicWithClientId throws an exception")
        inner class StartFlowCordaRpcOpsStartFlowDynamicWithClientIdThrowsExceptionSpec {

            @Test
            fun `should NOT call FlowResultsRepository anymore`() {
                whenever(cordaRpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verify(flowResultRepository, never()).updateFlowId(any(), any())
                verify(flowResultRepository, never()).update<String>(any(), any(), any(), any())
            }

            @Test
            fun `should NOT call CordaRPCOps removeClientId`() {
                whenever(cordaRpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verify(cordaRpcOps, never()).removeClientId(any())
            }

            @Test
            fun `should throw this exception`() {
                whenever(cordaRpcOps.startFlowDynamicWithClientId<String>(any(), any(), any())).thenThrow(testException)

                val exc = shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }
                exc shouldBe testException
            }
        }

        @Nested
        @DisplayName("when FlowResultsRepository on updateFlowId throws an exception")
        inner class StartFlowRepositoryUpdateFlowIdThrowsExceptionSpec {

            @Test
            fun `should call FlowResultsRepository update`() {
                whenever(flowResultRepository.updateFlowId(any(), any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verify(flowResultRepository).update(
                    eq(testClientId),
                    eq(flowIdValue),
                    eq(FlowStatus.COMPLETED),
                    eq(flowResult)
                )
            }

            @Test
            fun `should call CordaRPCOps removeClientId`() {
                whenever(flowResultRepository.updateFlowId(any(), any())).thenThrow(testException)

                shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }

                verify(cordaRpcOps).removeClientId(eq(testClientId))
            }

            @Test
            fun `should throw this exception`() {
                whenever(flowResultRepository.updateFlowId(any(), any())).thenThrow(testException)

                val exc = shouldThrow<Exception> { flowExecutor.startFlow(testClientId, flowName, flowParams) }
                exc shouldBe testException
            }
        }

        @Nested
        @DisplayName("when FlowResultsRepository on update throws an exception")
        inner class StartFlowRepositoryUpdateThrowsExceptionSpec {

            @Test
            fun `should NOT call CordaRPCOps removeClientId`() {
                whenever(flowResultRepository.update<String>(any(), any(), any(), any())).thenThrow(testException)

                flowExecutor.startFlow(testClientId, flowName, flowParams)

                verify(cordaRpcOps, never()).removeClientId(eq(testClientId))
            }

            @Test
            fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
                whenever(flowResultRepository.update<String>(any(), any(), any(), any())).thenThrow(testException)

                val result = flowExecutor.startFlow(testClientId, flowName, flowParams)

                result.clientId shouldBe testClientId
                result.flowId.uuid shouldBe flowIdValue
            }
        }

        @Nested
        @DisplayName("when CordaRPCOps on removeClientId throws an exception")
        inner class StartFlowCordaRpcOpsRemoveClientIdThrowsExceptionSpec {

            @Test
            fun `should return RpcStartFlowResponse with flowId returned from CordaRPCOps`() {
                whenever(cordaRpcOps.removeClientId(any())).thenThrow(testException)

                val result = flowExecutor.startFlow(testClientId, flowName, flowParams)

                result.clientId shouldBe testClientId
                result.flowId.uuid shouldBe flowIdValue
            }
        }
    }
}
