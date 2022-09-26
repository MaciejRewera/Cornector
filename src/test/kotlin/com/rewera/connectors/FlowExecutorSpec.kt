package com.rewera.connectors

import com.rewera.models.FlowResult
import com.rewera.models.api.FlowStatus
import com.rewera.models.api.RpcStartFlowRequestParameters
import com.rewera.repositories.FlowResultRepository
import com.rewera.testdata.TestData
import com.rewera.testdata.TestData.randomUuid
import com.rewera.testdata.TestData.testClientId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import net.corda.core.flows.StateMachineRunId
import net.corda.core.internal.concurrent.doneFuture
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandleWithClientIdImpl
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.*
import java.util.*

class FlowExecutorSpec {

    private val cordaRpcOps = Mockito.mock(CordaRPCOps::class.java)
    private val flowResultRepository = Mockito.mock(FlowResultRepository::class.java)
    private val flowClassBuilder = Mockito.mock(FlowClassBuilder::class.java)

    private val flowExecutor = FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

    private val flowResult = "Flow Result"
    private fun flowHandle(flowId: UUID, clientId: String) =
        FlowHandleWithClientIdImpl(StateMachineRunId(flowId), doneFuture(flowResult), clientId)

    private val testException = RuntimeException("Test Exception")

    @BeforeEach
    fun setup() {
        reset(cordaRpcOps, flowResultRepository, flowClassBuilder)
    }

    @Nested
    @DisplayName("FlowExecutor on initialisation")
    inner class InitialisationSpec {

        private val flowIdValue1 = randomUuid()
        private val testRunningFlowResult1 =
            FlowResult<Any>("client-id-1", flowId = flowIdValue1.toString(), status = FlowStatus.RUNNING)
        private val flowHandle1 = flowHandle(flowIdValue1, testRunningFlowResult1.clientId)

        private val flowIdValue2 = randomUuid()
        private val testRunningFlowResult2 =
            FlowResult<Any>("client-id-2", flowId = flowIdValue2.toString(), status = FlowStatus.RUNNING)
        private val flowHandle2 = flowHandle(flowIdValue2, testRunningFlowResult2.clientId)

        private val flowIdValue3 = randomUuid()
        private val testRunningFlowResult3 =
            FlowResult<Any>("client-id-3", flowId = flowIdValue2.toString(), status = FlowStatus.RUNNING)
        private val flowHandle3 = flowHandle(flowIdValue3, testRunningFlowResult3.clientId)

        @Test
        fun `should call FlowResultRepository to fetch all running flows`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(emptyList())

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(flowResultRepository).findByStatus(eq(FlowStatus.RUNNING))
        }

        @Test
        fun `when there are NO running flows should NOT call CordaRPCOps at all`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(emptyList())

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verifyNoInteractions(cordaRpcOps)
        }

        @Test
        fun `when there are NO running flows should NOT call FlowResultRepository update`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(emptyList())

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(flowResultRepository, never()).update<Any?>(any(), any(), any(), any())
        }

        @Test
        fun `when there is single running flow should call CordaRPCOps reattachFlowWithClientId`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(cordaRpcOps).reattachFlowWithClientId<Any?>(eq(testRunningFlowResult1.clientId))
        }

        @Test
        fun `when there is single running flow should call FlowResultRepository update`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(any())).thenReturn(flowHandle1)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(flowResultRepository).update(
                eq(testRunningFlowResult1.clientId),
                eq(flowIdValue1),
                eq(FlowStatus.COMPLETED),
                eq(flowResult)
            )
        }

        @Test
        fun `when there is single running flow should call CordaRPCOps removeClientId`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(any())).thenReturn(flowHandle1)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(cordaRpcOps).removeClientId(eq(testRunningFlowResult1.clientId))
        }

        @Test
        fun `when there is single running flow but CordaRPCOps returns null should NOT call FlowResultRepository update`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
            whenever(cordaRpcOps.reattachFlowWithClientId<Any?>(any())).thenReturn(null)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(flowResultRepository, never()).update<Any?>(any(), any(), any(), any())
        }

        @Test
        fun `when there is single running flow but CordaRPCOps returns null should NOT call CordaRPCOps removeClientId`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
            whenever(cordaRpcOps.reattachFlowWithClientId<Any?>(any())).thenReturn(null)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(cordaRpcOps, never()).removeClientId(any())
        }

        @Test
        fun `when there are multiple running flows should call CordaRPCOps reattachFlowWithClientId for each flow`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(
                listOf(testRunningFlowResult1, testRunningFlowResult2, testRunningFlowResult3)
            )

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(cordaRpcOps).reattachFlowWithClientId<Any?>(eq(testRunningFlowResult1.clientId))
            verify(cordaRpcOps).reattachFlowWithClientId<Any?>(eq(testRunningFlowResult2.clientId))
            verify(cordaRpcOps).reattachFlowWithClientId<Any?>(eq(testRunningFlowResult3.clientId))
        }

        @Test
        fun `when there are multiple running flows should call FlowResultRepository update for each flow`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(
                listOf(testRunningFlowResult1, testRunningFlowResult2, testRunningFlowResult3)
            )
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult1.clientId)))
                .thenReturn(flowHandle1)
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult2.clientId)))
                .thenReturn(flowHandle2)
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult3.clientId)))
                .thenReturn(flowHandle3)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(flowResultRepository).update(
                eq(testRunningFlowResult1.clientId),
                eq(flowIdValue1),
                eq(FlowStatus.COMPLETED),
                eq(flowResult)
            )
            verify(flowResultRepository).update(
                eq(testRunningFlowResult2.clientId),
                eq(flowIdValue2),
                eq(FlowStatus.COMPLETED),
                eq(flowResult)
            )
            verify(flowResultRepository).update(
                eq(testRunningFlowResult3.clientId),
                eq(flowIdValue3),
                eq(FlowStatus.COMPLETED),
                eq(flowResult)
            )
        }

        @Test
        fun `when there are multiple running flows should call CordaRPCOps removeClientId for each flow`() {
            whenever(flowResultRepository.findByStatus(any())).thenReturn(
                listOf(testRunningFlowResult1, testRunningFlowResult2, testRunningFlowResult3)
            )
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult1.clientId)))
                .thenReturn(flowHandle1)
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult2.clientId)))
                .thenReturn(flowHandle2)
            whenever(cordaRpcOps.reattachFlowWithClientId<String>(eq(testRunningFlowResult3.clientId)))
                .thenReturn(flowHandle3)

            FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

            verify(cordaRpcOps).removeClientId(eq(testRunningFlowResult1.clientId))
            verify(cordaRpcOps).removeClientId(eq(testRunningFlowResult2.clientId))
            verify(cordaRpcOps).removeClientId(eq(testRunningFlowResult3.clientId))
        }

        @Nested
        @DisplayName("when there is running flow but CordaRPCOps on reattachFlowWithClientId throws an exception")
        inner class InitialisationReattachFlowWithClientIdThrowsExceptionSpec {

            @Test
            fun `should NOT throw this exception`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<Any?>(any())).thenThrow(testException)

                assertDoesNotThrow { FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder) }
            }

            @Test
            fun `should NOT call FlowResultRepository update`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<Any?>(any())).thenThrow(testException)

                FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

                verify(flowResultRepository, never()).update<Any?>(any(), any(), any(), any())
            }

            @Test
            fun `should NOT call CordaRPCOps removeClientId`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<Any?>(any())).thenThrow(testException)

                FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

                verify(cordaRpcOps, never()).removeClientId(any())
            }
        }

        @Nested
        @DisplayName("when there is running flow but FlowResultRepository on update throws an exception")
        inner class InitialisationRepositoryUpdateThrowsExceptionSpec {

            @Test
            fun `should NOT throw this exception`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<String>(any())).thenReturn(flowHandle1)
                whenever(flowResultRepository.update<String>(any(), any(), any(), any())).thenThrow(testException)

                assertDoesNotThrow { FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder) }
            }

            @Test
            fun `should NOT call CordaRPCOps removeClientId`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<String>(any())).thenReturn(flowHandle1)
                whenever(flowResultRepository.update<String>(any(), any(), any(), any())).thenThrow(testException)

                FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder)

                verify(cordaRpcOps, never()).removeClientId(any())
            }
        }

        @Nested
        @DisplayName("when there is running flow but CordaRPCOps on removeClientId throws an exception")
        inner class InitialisationRemoveClientIdThrowsExceptionSpec {

            @Test
            fun `should NOT throw this exception`() {
                whenever(flowResultRepository.findByStatus(any())).thenReturn(listOf(testRunningFlowResult1))
                whenever(cordaRpcOps.reattachFlowWithClientId<String>(any())).thenReturn(flowHandle1)
                whenever(cordaRpcOps.removeClientId(any())).thenThrow(testException)

                assertDoesNotThrow { FlowExecutor(cordaRpcOps, flowResultRepository, flowClassBuilder) }
            }
        }
    }

    @Nested
    @DisplayName("FlowExecutor on startFlow")
    inner class StartFlowSpec {

        private val flowName = TestData.SingleParameterTestFlow::class.java.name
        private val flowIdValue = randomUuid()
        private val flowHandle =
            FlowHandleWithClientIdImpl(StateMachineRunId(flowIdValue), doneFuture(flowResult), testClientId)
        private val flowParams = RpcStartFlowRequestParameters("This should be a JSON")

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
